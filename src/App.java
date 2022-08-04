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

    public static void initSyms() {
        symObj.put("pi", String.valueOf(Math.PI));
        // More symbols
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
                    System.out.println(func);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')'))
                            throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt"))
                        x = Math.sqrt(x);
                    else if (func.equals("sin"))
                        x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos"))
                        x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan"))
                        x = Math.tan(Math.toRadians(x));
                    else if (func.equals("asin"))
                        x = Math.toDegrees(Math.asin(x));
                    else if (func.equals("acos"))
                        x = Math.toDegrees(Math.acos(x));
                    else if (func.equals("atan"))
                        x = Math.toDegrees(Math.atan(x));
                    else if (func.equals("log"))
                        x = Math.log(x);
                    else
                        throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                    x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static String replaceSym() {
        return textField.getText().replaceAll("pi", String.valueOf(Math.PI));
    }

    public void keyPressed(KeyEvent e) {
        if (KeyEvent.getKeyText(e.getKeyCode()).equals("⏎")) {
            prevEquation = textField.getText();
            try {
                textField.setText(String.valueOf(eval(replaceSym())));
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
        initSyms();
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
