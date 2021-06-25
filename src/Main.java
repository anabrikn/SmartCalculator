package calculator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static HashMap<String, String> map = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str;

        while (true) {
            str = scanner.nextLine(); // считываем строку

            if (str.isEmpty()) {
                continue;
            }

            // блок проверяет ввод команды
            if (str.startsWith("/")) {
                if (str.equals("/exit")) {
                    System.out.println("Bye!");
                    break;
                }
                if (str.equals("/help")) {
                    System.out.println("Consider that the even number of minuses gives a plus,");
                    System.out.println("and the odd number of minuses gives a minus!");
                    System.out.println("Look at it this way: 2 -- 2 equals 2 - (-2) equals 2 + 2.");
                    continue;
                }

                if (str.equals("/lookvar")) {
                    System.out.println(map.entrySet().toString());
                    continue;
                }
                System.out.println("Unknown command");
                continue;
            }

            // в этом блоке происходит запись переменных и их значений в мапу
            if (str.matches(".*=.*")) {

                try {
                    String[] keyValue = str.replaceAll("\\s*", "").split("=");

                    if (keyValue.length > 2 || !keyValue[1].matches("([0-9]+|[a-zA-Z]+)")) {
                        System.out.println("Invalid assignment");
                        continue;
                    }

                    if (str.matches("[a-zA-Z]+\\s*=\\s*[0-9]+")) { // соответствует ли присвоению числового значения
                        map.put(keyValue[0], keyValue[1]);
                        continue;
                    }

                    if (str.matches("[a-zA-Z]+\\s*=\\s*[a-zA-Z]+")) { // соответствует ли присвоению значения другой перемнной
                        if (!map.containsKey(keyValue[1])) {
                            System.out.println("Unknown variable");
                            continue;
                        }
                        map.put(keyValue[0], map.get(keyValue[1]));
                        continue;
                    }

                    System.out.println("Invalid identifier");

                } catch (Exception e) {
                    System.out.println("???");
                }

            }

            try {
                String prepared = preparingExpression(str);
                String rpn = expressionToRPN(prepared);
                System.out.println(rpnToAnswer(rpn));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    // в этом методе мы вставляем ноль перед отрицательными числами если они в начале строки или сразу после открывающей скобки
    // или если все выражение - одно число
    // так же проверяем баланс скобок
    private static String preparingExpression(String expression) throws Exception {
        expression = expression.replaceAll("^", "0+"); // добавляет + в самое начало строки
        expression = expression.replaceAll("\\s+", ""); // все пробелы удаляет
        expression = expression.replaceAll("(\\++|(--)+)", "+"); //эти выражения приводят строку к стандартному виду, заменяя подстроки на +
        expression = expression.replaceAll("(\\+-|-(--)*)", "-"); //эти выражения приводят строку к стандартному виду, заменяя подстроки на -

        Pattern p = Pattern.compile("([a-zA-Z]+)+?"); //паттерн на выявление текстовой переменной
        Matcher m = p.matcher(expression);
        while (m.find()) {
            String s = m.group();
            if (map.containsKey(s)) {
                expression = expression.replaceFirst("([a-zA-Z]+)+?", map.get(s));
            } else {
                throw new Exception("Unknown variable (Такой переменной не существует) " + s);
            }
        }

        Pattern manyDivOrMultSymbols = Pattern.compile("(\\*(\\*)+|/(/)+)+?"); //паттерн на выявление числа со знаком перед ним
        Matcher matcher = manyDivOrMultSymbols.matcher(expression);
        if (matcher.find()) {
            throw new Exception("Invalid expression");
        }

        if (isBalance(expression) != true) {
            throw new Exception("Invalid expression");
        }

        String preparedExpression = new String();
        for (int token = 0; token < expression.length(); token++) {
            char symbol = expression.charAt(token);
            if (symbol == '-') {
                if (token == 0) {
                    preparedExpression += '0';
                } else if (expression.charAt(token - 1) == '(') {
                    preparedExpression += 0;
                }
            }
            preparedExpression += symbol;
        }

        return preparedExpression;
    }

    // метод, проверяющий баланс скобок
    private static boolean isBalance(String exp) {
        Stack<Character> stack = new Stack<>();
        char[] seq = exp.toCharArray();
        for (int i = 0; i < exp.length(); i++) {
            if(seq[i] == '(') {
                stack.push(seq[i]);
            }

            if(seq[i] == ')') {
                if (!stack.empty() && stack.peek() == '(') {
                    stack.pop();
                } else {
                    return false;
                }
            }
        }
        return stack.empty();
    }

    // форматирует выражение в обратную польскую нотацию
    private static String expressionToRPN(String expr) {
        String current = "";                // строка результат
        Stack<Character> stack = new Stack<>(); // создаем стек

        int priority;
        for (int i = 0; i < expr.length(); i++) {    // цикл для обхода строки
            char currentChar = expr.charAt(i);       // берем очередной сомвол строки по индексу i
            priority = getPriority(currentChar);     // узнаем его приоритет

            if (priority == 0) {            // ЕСЛИ цифра
                current += currentChar;     // просто лепим к результирующей строке
            }
            if (priority == 1) {            // ЕСЛИ открывающая скобка - (
                stack.push(currentChar);    // помещаем скобку в стек
            }

            if (priority > 1) { // ЕСЛИ  + = приоритет 2      * / приоритет 3
                current += ' '; // добавляет результируещей строке пробел
                while (!stack.empty()) { // пока стек не опустеет
                    // ниже получаем приоритет самого верхнего элемента стека и сравниваем с пр. текущего символа (currentChar)
                    if (getPriority(stack.peek()) >= priority) {  // если приоритет верхнего элемента стека выше приоритета текущего элемента, или равен
                        current += stack.pop(); // то извлекаем этот верхний элемент из стека и добавляем в результ-строку
                    } else {
                        break;
                    }
                }
                stack.push(currentChar); // помещаем текущий элемент в стек
            }

            if (priority == -1) { // ЕСЛИ закрывающая скобка - )
                current += ' '; // добавляем пробел к резалт-строке
                while (getPriority(stack.peek()) != 1) { // пока символ стека не открывающая скобка -
                    current += stack.pop(); // извлекаем этот символ из стека и прибавляем к результ-строке
                }
                stack.pop(); // удаляем верхний символ из стека - открывающую скобку
            }
        } // выход из цикла for - мы обошли всю строку

        while (!stack.empty()) { // пока стек не пуст
            current += stack.pop(); // извлекаем сверху по символу и добавляем к результ-строке
        }

        // печать строки в польской обратной нотации
        return current; // возвращаем результ-строку - выражение в обратной нотации
    }

    // вычисление результата выражения
    private static BigInteger rpnToAnswer(String rpn) {
        String operand = new String();
        Stack<BigInteger> stack = new Stack<>();

        for (int i = 0; i < rpn.length(); i++) {
            if (rpn.charAt(i) == ' ') {
                continue;
            }
            if (getPriority(rpn.charAt(i)) == 0) {
                while (rpn.charAt(i) != ' ' && getPriority(rpn.charAt(i)) == 0) {
                    operand += rpn.charAt(i++);
                    if (i == rpn.length()) {
                        break;
                    }
                }
                stack.push(new BigInteger(operand));
                operand = new String();
            }
            if (getPriority(rpn.charAt(i)) > 1) {
                BigInteger a = stack.pop();
                BigInteger b = stack.pop();
                if (rpn.charAt(i) == '+') {
                    stack.push(b.add(a));
                }
                if (rpn.charAt(i) == '-') {
                    stack.push(b.subtract(a));
                }
                if (rpn.charAt(i) == '*') {
                    stack.push(b.multiply(a));
                }
                if (rpn.charAt(i) == '/') {
                    stack.push(b.divide(a));
                }
            }
        }

        return stack.pop();
    }

    // получаем приоритет символа
    private static int getPriority(char token) {
        if (token == '*' || token == '/') {
            return 3;
        } else if(token == '+' || token == '-') {
            return 2;
        } else if (token == '(') {
            return 1;
        } else if (token == ')') {
            return -1;
        } else {
            return 0;
        }
    }
}