package com.mwendasoft.bittowl;

import java.util.Stack;

public class ExpressionEvaluator {

    public double evaluate(String expr) {
        expr = preprocessExpression(expr);
        String postfix = convertToPostfix(expr);
        return evaluatePostfix(postfix);
    }

    // Preprocess expression: handle unary minus and % as division by 100
    private String preprocessExpression(String expr) {
        StringBuilder result = new StringBuilder();
        char prev = ' ';  // Track previous char to detect unary minus

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '%') {
                result.append("/100");
                prev = '0'; // treat % as operator replaced
                continue;
            }

            if (c == '-') {
                // Unary minus detection
                if (i == 0 || prev == '(' || isOperator(prev)) {
                    result.append("0-");
                    prev = '-';
                    continue;
                }
            }

            result.append(c);
            prev = c;
        }

        return result.toString();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private boolean isFunction(String token) {
        return token.equals("sin") || token.equals("cos") || token.equals("tan")
			|| token.equals("log") || token.equals("ln")
			|| token.equals("sqrt") || token.equals("cbrt");
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        if (op == '^') return 3;
        return 0;
    }

    private String convertToPostfix(String infix) {
        StringBuilder output = new StringBuilder();
        Stack<String> stack = new Stack<>();
        int i = 0;

        while (i < infix.length()) {
            char c = infix.charAt(i);

            if (c == ' ') {
                i++;
                continue;
            }

            // Number with decimal support
            if (Character.isDigit(c) || c == '.') {
                while (i < infix.length() &&
					   (Character.isDigit(infix.charAt(i)) || infix.charAt(i) == '.')) {
                    output.append(infix.charAt(i));
                    i++;
                }
                output.append(' ');
                continue;
            }

            // Function (letters)
            if (Character.isLetter(c)) {
                StringBuilder func = new StringBuilder();
                while (i < infix.length() && Character.isLetter(infix.charAt(i))) {
                    func.append(infix.charAt(i));
                    i++;
                }
                stack.push(func.toString());
                continue;
            }

            // Parentheses
            if (c == '(') {
                stack.push("(");
            } else if (c == ')') {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.append(stack.pop()).append(' ');
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop(); // remove '('
                }
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    output.append(stack.pop()).append(' ');
                }
            }
            // Operators
            else if (isOperator(c)) {
                String op = String.valueOf(c);
                while (!stack.isEmpty() && isOperator(stack.peek().charAt(0)) &&
					   ((c != '^' && precedence(stack.peek().charAt(0)) >= precedence(c)) ||
					   (c == '^' && precedence(stack.peek().charAt(0)) > precedence(c)))) {
                    output.append(stack.pop()).append(' ');
                }
                stack.push(op);
            }

            i++;
        }

        while (!stack.isEmpty()) {
            output.append(stack.pop()).append(' ');
        }

        return output.toString();
    }

    private double evaluatePostfix(String postfix) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = postfix.split(" ");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (isOperator(token.charAt(0)) && token.length() == 1) {
                double b = stack.pop();
                double a = stack.pop();
                switch (token.charAt(0)) {
                    case '+': stack.push(a + b); break;
                    case '-': stack.push(a - b); break;
                    case '*': stack.push(a * b); break;
                    case '/': stack.push(a / b); break;
                    case '^': stack.push(Math.pow(a, b)); break;
                }
            }
            else if (isFunction(token)) {
                double a = stack.pop();
                if (token.equals("sin")) stack.push(Math.sin(Math.toRadians(a)));
                else if (token.equals("cos")) stack.push(Math.cos(Math.toRadians(a)));
                else if (token.equals("tan")) stack.push(Math.tan(Math.toRadians(a)));
                else if (token.equals("log")) stack.push(Math.log10(a));
                else if (token.equals("ln")) stack.push(Math.log(a));
                else if (token.equals("sqrt")) stack.push(Math.sqrt(a));
                else if (token.equals("cbrt")) stack.push(Math.cbrt(a));
            }
            else {
                stack.push(Double.parseDouble(token));
            }
        }

        return stack.pop();
    }
}
