import java.util.*;

import static java.lang.System.out;

public class Calculator {
    public enum OperationTokens {
        plus, subtract, multiply, divide, mod, integer, power, openparen, closeparen, NIL
    }

    public static class Token {
        int priority;
        OperationTokens token;
        long integer;
        String string;

        // constructor for operations
        public Token(OperationTokens operation, int priority, String str) {
            this.priority = priority;
            this.token = operation;

            this.string = str;

        }

        // constructor for integers
        public Token(String integer) {
            this.integer = Long.parseLong(integer);
            this.token = OperationTokens.integer;
            this.string = integer;
        }

        public boolean isOperation() {
            return this.token != OperationTokens.integer;

        }

        public long getInteger() {

                return this.integer;

        }

        public OperationTokens getToken() {
            return this.token;
        }

        public int getPriority() {
            return this.priority;
        }


    }

    public static Token getToken(String input) {
        Token token;
        switch (input) {
            case "+":
                token = new Token(OperationTokens.plus, 1, input);
                break;
            case "-":
                token = new Token(OperationTokens.subtract, 1, input);
                break;
            case "*":
                token = new Token(OperationTokens.multiply, 2, input);
                break;
            case "/":
                token = new Token(OperationTokens.divide, 2, input);
                break;
            case "%":
                token = new Token(OperationTokens.mod, 2, input);
                break;
            case " ^":
                token = new Token(OperationTokens.power, 3, input);
                break;
            case "(":
                token = new Token(OperationTokens.openparen, 0, input);
                break;
            case ")":
                token = new Token(OperationTokens.closeparen, 0, input);
                break;
            default:
                token = new Token(input);
                break;


        }
        return token;
    }

    public static List<Token> stringToToken(String inputstring) {
        List<Token> stringtotoken = new ArrayList<>();
        // process input string
        String trimstring = inputstring.trim();
        String[] splitstring = trimstring.split("\\s+");
        //creates list of tokens correspondind to the string
        for (String character : splitstring) {
            Token tok = getToken(character);
            stringtotoken.add(tok);
        }
        return stringtotoken;

    }

    public static List<Token> getPostfix(List<Token> infix) {
        List<Token> postfix = new LinkedList<>();
        ArrayDeque<Token> priorityq = new ArrayDeque<>();// used like a stack
        Iterator<Token> iterator = infix.iterator();
        priorityq.push(new Token(OperationTokens.NIL, 0, "$"));
        //shunt-yard algorithm
        while (iterator.hasNext()) { //while queue is not empty
            Token token = iterator.next();

            if (token.isOperation()) {// if a number, add to post fix list
                postfix.add(token);

            } else {//else

                if (token.getToken() == OperationTokens.openparen) { // if it is open parenthesis, add to pq
                    priorityq.push(token);
                } else if (token.getToken() == OperationTokens.closeparen) {// if next token is close paren, postfix.add up until open parenthesis
                    while (priorityq.peek().token != OperationTokens.openparen) {
                        postfix.add(priorityq.pop());
                    }
                    priorityq.pop();// discard open parenthesis

                } else if (token.getPriority() > priorityq.peek().getPriority()) {// if current tokens priority is less than top of pq token, add to queue
                    priorityq.push(token);

                } else if (token.getPriority() <= priorityq.peek().getPriority()) {// else current tokens priority less than top of stack
                    while (priorityq.peek() != null && token.getPriority() <= priorityq.peek().getPriority()) {
                        postfix.add(priorityq.pop());
                    }
                    priorityq.push(token);
                }


            }

        }
        while (priorityq.peek() != null && priorityq.peek().getToken() != OperationTokens.NIL) {
            postfix.add(priorityq.pop());
        }

        return postfix;
    }

    public static long evaluate(List<Token> postfix) {
        Iterator<Token> iterator = postfix.iterator();// iterate trough the list of tokens
        ArrayDeque<Token> stack = new ArrayDeque<>();//stack to hold operands and answer
        long answer = 0; // store answer
        //while next token is not null
        while (iterator.hasNext()) {
            Token tok = iterator.next();
            if (tok.isOperation()) {// if it is an operand, push to stack
                stack.push(tok);
            } else {// else the token is an operator and performs the operator on the last two operators, pushes answer to the stack

                long rightoperand = stack.pop().getInteger(); // pop enough operands, the top two numbers from the stack, right and left
                long leftoperand = stack.pop().getInteger();
                // check wich operator, do the right operation then push to stack
                if (tok.token == OperationTokens.plus) {
                    answer = leftoperand + rightoperand;
                    stack.push(new Token("" + answer));

                } else if (tok.token == OperationTokens.subtract) {
                    answer = leftoperand - rightoperand;
                    stack.push(new Token("" + answer));
                } else if (tok.token == OperationTokens.multiply) {
                    answer = leftoperand * rightoperand;
                    stack.push(new Token("" + answer));
                } else if (tok.token == OperationTokens.divide) {
                    answer = leftoperand / rightoperand;
                    stack.push(new Token("" + answer));
                } else if (tok.token == OperationTokens.mod) {
                    answer = leftoperand % rightoperand;
                    stack.push(new Token("" + answer));
                } else {
                    answer = (long) Math.pow(leftoperand, rightoperand);
                    stack.push(new Token("" + answer));
                }
            }
        }
        out.println(stack.pop().getInteger());
        return stack.pop().getInteger();// top of stack should hold our answer
    }

    public static long calculate(String input) {
        List<Token> infix = stringToToken(input);
        List<Token> postfix = getPostfix(infix);
        return evaluate(postfix);

    }
}
