import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class App implements KeyListener {
    public static JFrame frame = new JFrame();
    public static JPanel panel = new JPanel();
    public static JTextField textField = new JTextField();
    public static String prevEquation = "";
    public static char prevKey = ' ';
    public static HashMap<String, String> symObj = new HashMap<String, String>();

    public static void initCustoms() {
        symObj.put("pi", String.valueOf(Math.PI));
        symObj.put("dist(", String.valueOf("sqrt((var1 - var3)^2 + (var2 - var4)^2)"));
    }

    public static void correctPointError(double num) {
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            // | functionName `(` expression `)` | functionName factor
            // | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+'))
                        x += parseTerm(); // addition
                    else if (eat('-'))
                        x -= parseTerm(); // subtraction
                    else
                        return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*'))
                        x *= parseFactor(); // multiplication
                    else if (eat('/'))
                        x /= parseFactor(); // division
                    else
                        return x;
                }
            }

            double parseFactor() {
                if (eat('+'))
                    return +parseFactor(); // unary plus
                if (eat('-'))
                    return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')'))
                        throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z')
                        nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')'))
                            throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        case "asin":
                            x = Math.toDegrees(Math.asin(x));
                        case "atan":
                            x = Math.toDegrees(Math.atan(x));
                            break;
                        case "acos":
                            x = Math.toDegrees(Math.acos(x));
                            break;
                        case "log":
                            x = Math.log(x);
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                    x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static void recursivelyParseCustoms(Object[] symArr) {
        for (int i = 0; i < symArr.length; i++) {
            if (textField.getText().contains(String.valueOf(symArr[i]))) {
                // Function Logics
                if (String.valueOf(symArr[i]).contains("(")) {
                    int startingIdx = textField.getText().indexOf(String.valueOf(symArr[i]))
                            + String.valueOf(symArr[i]).length();
                    for (int x = startingIdx; x < textField.getText().length(); x++) {
                        if (textField.getText().charAt(x) == ')') {
                            String args = textField.getText().substring(startingIdx, x);
                            String[] tempArr = args.split(",");
                            String updatedStr = String.valueOf(symObj.get(String.valueOf(symArr[i])));
                            for (byte j = 0; j < tempArr.length; j++)
                                updatedStr = updatedStr.replace("var" + (j + 1), tempArr[j]);
                            if (updatedStr.contains("var"))
                                throw new RuntimeException("Wrong number of arguments");
                            textField.setText(textField.getText().replace(textField.getText().substring(
                                    textField.getText().indexOf(String.valueOf(symArr[i])), x + 1), updatedStr));
                            break;
                        }
                    }
                    // Symbol Logics
                } else
                    textField.setText(textField.getText().replaceAll((String) symArr[i], symObj.get(symArr[i])));
                // Recursion to find same customs (e.g dist(1, 2, 3, 4) + dist(1,2, 5, 6))
                recursivelyParseCustoms(symArr);
            } else
                break;
        }
    }

    public void keyPressed(KeyEvent e) {
        if (KeyEvent.getKeyText(e.getKeyCode()).equals("⏎")) {
            prevEquation = textField.getText();
            try {
                Object[] symArr = symObj.keySet().toArray();
                recursivelyParseCustoms(symArr);
                textField.setText(String.valueOf(eval(textField.getText())));
            } catch (Exception ex) {
                textField.setText("Error: " + ex);
            }
        } else if (KeyEvent.getKeyText(e.getKeyCode()).equals("↑")) {
            textField.setText(prevEquation);
        } else if ((prevKey == '⌘' || prevKey == '⌃') && KeyEvent.getKeyText(e.getKeyCode()).equals("⌫")) {
            textField.setText("");
        } else {
            prevKey = KeyEvent.getKeyText(e.getKeyCode()).charAt(0);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void initialize() {
        initCustoms();
        textField.setFont(new Font("Monospaced", 0, 20));
        textField.addKeyListener(this);

        frame.setLayout(new GridLayout(1, 0));
        frame.add(textField);
        frame.setTitle("Text Calculator");
        frame.setPreferredSize(new Dimension(300, 75));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
    }

    public static void main(String[] args) throws Exception {
        new App().initialize();
    }
}
