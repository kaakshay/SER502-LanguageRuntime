import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MinVM {

    public static enum TokenType {
        COMMENT("--.*\\n"),  STRING("\"(\\.|[^\"])*\""), INTEGER ("(\\+|-)?[0-9]+"),BOOLEAN ( "true|false"),
        IDENTIFIER ( "[A-Za-z_][A-Za-z0-9]*"), OPERATOR ( "\\*|\\+|\\-|\\/|\\&|\\||\\~|\\^|\\<\\=|\\>\\=|\\<|\\>|\\="),
        QUOTEDBLOCK( "@\\["), UNQUOTEDBLOCK("\\["), BLOCKEND("]"), BANG("\\!");

        public final String pattern;
        private TokenType(String pattern){
            this.pattern = pattern;
        }
    }

    public static class Token{
        TokenType type;
        String stringValue;

        public Token(TokenType type, String token) {
            this.type = type;
            this.stringValue = token;
        }
    }

    class Tokenizer implements Iterator<String> {
        private final BufferedReader source;
        Queue<Token> tokens = new ArrayDeque<>();
        Pattern pattern;

        Tokenizer(String s) {
            this(new BufferedReader(new StringReader(s)));
        }

        Tokenizer(Reader source) {
            this(new BufferedReader(source));
        }

        Tokenizer(BufferedReader source) {
            this.source = source;

            StringBuilder regex = new StringBuilder();
            for (TokenType t: TokenType.values()) {
                regex.append("|(?<").append(t.name()).append(">").append(t.pattern).append(")");
            }
            this.pattern = Pattern.compile(regex.substring(1)); // skip the first |
        }

        void tokenize(String src){

            Matcher matcher = this.pattern.matcher(src);
            while (matcher.find()) {
                String tv;
                for (TokenType tt : TokenType.values()){
                    if ( (tv = matcher.group(tt.name())) != null) {
                        tokens.add(new Token(tt, tv));
                        break;
                    }
                }
            }

        }

        public boolean hasNext() {

            while (tokens.isEmpty()) {
                String line = null;

                try {
                    line = source.readLine();
                } catch (IOException e) {
                    return false;
                }

                if (line == null)
                    return false;
                else {
                    tokenize(line);
                }
            }

            return true;
        }

        public String next() {
            if (!hasNext()) {
                return null;
            } else {
                if (trace) {
                    System.out.println("-->" +  tokens.element().stringValue);
                    showEnvironment();
                }
                return tokens.remove().stringValue;
            }
        }

        TokenType nextType() {
            if (!hasNext()) {
                return null;
            } else {
                return tokens.element().type;
            }
        }
    }

    class SymbolEntry{
        String name;
        String value;
        SymbolEntry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    class SymbolTable{
        LinkedList<HashMap<String, SymbolEntry> > symbols = new LinkedList< HashMap<String, SymbolEntry>>();

        SymbolEntry get(String symbol) {
            for (HashMap<String, SymbolEntry> table: symbols) {
                if (table.containsKey(symbol)) {
                    return table.get(symbol);
                }
            }
            error("Attempted to access undefined symbol: "+ symbol);
            return null;
        }

        void bind(String name, String initialValue) {
            symbols.getFirst().put(name, new SymbolEntry(name, initialValue));
            if (trace) {
                showEnvironment();
            }
        }

        void enter() {
            symbols.addFirst(new HashMap<String, SymbolEntry>());
            if (trace) showEnvironment();
        }

        void exit() {
            symbols.removeFirst();
            if (trace) showEnvironment();
        }
     }


    Stack<String> stack = new java.util.Stack<String>();
    SymbolTable symbols = new SymbolTable();
    LineNumberReader source = null;
    Boolean trace = false;
    String filename = "<stdin>";


    MinVM(File src) throws FileNotFoundException {
        this(new FileReader(src));
        this.filename = src.getName();
    }

    MinVM(InputStream src) {
        this(new InputStreamReader(src));
    }

    MinVM(Reader src) {
        source = new LineNumberReader(src);
    }


    void error(String message) {
        throw new RuntimeException("ERROR(" + source.getLineNumber() + "):" + message);
    }


    void showEnvironment() {
        System.out.println(" ,:");
        for (HashMap<String, SymbolEntry> scope : symbols.symbols){
            System.out.println(":::   scope:" + scope);
        }
        System.out.println(":::");
        System.out.println(":::   stack" + stack);
        System.out.println(" `:");
    }

    void push(String value) {
        stack.push(value);
        if (trace)
            showEnvironment();
    }

    String pop() {
        String result = stack.pop();
        if (trace)
            showEnvironment();
        return result;
    }


    void operator(Tokenizer scanner) {
        String op = scanner.next();

        if (op.equals("~")) {  // not operator (unary)
            push(String.valueOf(!Boolean.parseBoolean(pop())));
        } else {

            //Binary operators +-/*  &|^   (^ is exclusive or)
            String s2 = pop();
            String s1 = pop();
            switch (op) {
                case "+":
                    push(String.valueOf(Integer.parseInt(s1) + Integer.parseInt(s2)));
                    break;
                case "-":
                    push(String.valueOf(Integer.parseInt(s1) - Integer.parseInt(s2)));
                    break;
                case "*":
                    push(String.valueOf(Integer.parseInt(s1) * Integer.parseInt(s2)));
                    break;
                case "/":
                    push(String.valueOf(Integer.parseInt(s1) / Integer.parseInt(s2)));
                    break;

                case "<":
                    push(String.valueOf(Integer.parseInt(s1) < Integer.parseInt(s2)));
                    break;
                case ">":
                    push(String.valueOf(Integer.parseInt(s1) > Integer.parseInt(s2)));
                    break;
                case "=":
                    push(String.valueOf(Integer.parseInt(s1) == Integer.parseInt(s2)));
                    break;
                case "<=":
                    push(String.valueOf(Integer.parseInt(s1) <= Integer.parseInt(s2)));
                    break;
                case ">=":
                    push(String.valueOf(Integer.parseInt(s1) >= Integer.parseInt(s2)));
                    break;


                case "&":
                    push(String.valueOf(Boolean.parseBoolean(s1) & Boolean.parseBoolean(s2)));
                    break;
                case "|":
                    push(String.valueOf(Boolean.parseBoolean(s1) | Boolean.parseBoolean(s2)));
                    break;
                case "^":
                    push(String.valueOf(Boolean.parseBoolean(s1) ^ Boolean.parseBoolean(s2)));
                    break;

            }
        }
    }

    void run() {
        //The start rule is 'block'
        block(new Tokenizer(source));
    }

    String unquoted(String s) {
        return s.substring(1, s.length()-1);
    }

    void block(Tokenizer scanner) {

        if (scanner.nextType() ==  TokenType.UNQUOTEDBLOCK) {
            scanner.next();
            symbols.enter();

            while (scanner.nextType() != TokenType.BLOCKEND) {
                switch (scanner.nextType()){
                    case COMMENT:
                        scanner.next();
                        break;
                    case STRING:
                        // I encountered an issue that the quot characters were part of the string
                        // This is a hackish way to allow escaped quotes...
                        push(unquoted(scanner.next()).replace("\\\"", "\""));
                        break;
                    case INTEGER:
                    case BOOLEAN:
                        push(scanner.next());
                        break;
                    case IDENTIFIER:
                        String symbol = scanner.next();

                        //Builtin functions
                        switch (symbol) {
                            case "?":
                                showEnvironment();
                                break;
                            case "show":
                                System.out.println(pop());
                                break;
                            case "pop":
                                pop();
                                break;
                            case "bind":
                            case "def":
                                symbols.bind(pop(), pop());
                                break;
                            case "tron":
                                trace = true;
                                break;
                            case "troff":
                                trace = false;
                                break;
                            case "if":
                                String cond = pop();
                                String falsePart = pop();
                                String truePart = pop();
                                switch (cond) {
                                    case "true":
                                        push(truePart);
                                        break;
                                    case "false":
                                        push(falsePart);
                                        break;
                                    default:
                                        error("The condition if an 'if' must be boolean.");
                                        break;
                                }
                                break;
                            case "load":
                                String file = pop();
                                System.out.println("loading " + new File(file).getAbsolutePath());
                                try {
                                    block(new Tokenizer(new FileReader(file)));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();

                                }
                                break;
                            default:
                                push(symbols.get(symbol).value);
                        }
                        break;
                    case QUOTEDBLOCK:
                        StringBuffer buffer = new StringBuffer();
                        quoted_block(scanner, buffer);
                        push(buffer.substring(1));  //Remove the quote
                        break;
                    case UNQUOTEDBLOCK:
                        block(scanner);
                        break;
                    case BANG:
                        scanner.next();
                        block(new Tokenizer(pop()));
                        break;
                    case OPERATOR:
                        operator(scanner);
                        break;
                    default:
                        error("Syntax: " + scanner.next());
                }
            }
            symbols.exit();
            scanner.next(); //BLOCKEND
        }
    }

    void quoted_block(Tokenizer scanner, StringBuffer buffer) {
        if (scanner.nextType() == TokenType.QUOTEDBLOCK | scanner.nextType() == TokenType.UNQUOTEDBLOCK){
            buffer.append(scanner.next());
            while (scanner.nextType() != TokenType.BLOCKEND){
                switch (scanner.nextType()) {
                    case COMMENT:
                        scanner.next();
                        break;
                    case STRING:
                    case INTEGER:
                    case BOOLEAN:
                        buffer.append(scanner.next()).append(' ');
                        break;
                    case IDENTIFIER:
                        buffer.append(scanner.next()).append(' ');
                        break;
                    case QUOTEDBLOCK:
                    case UNQUOTEDBLOCK:
                        quoted_block(scanner, buffer);
                        buffer.append(' ');
                        break;
                    case BANG:
                        buffer.append(scanner.next()).append(' ');
                        break;
                    case OPERATOR:
                        buffer.append(scanner.next()).append(' ');
                        break;
                    default:
                        error("Syntax");
                }
            }

            buffer.append(scanner.next());
        }
    }

    public static void main(String[] args) {
        MinVM vm = new MinVM(System.in);
        try {
            vm.run();
        } catch (RuntimeException e) {

            vm.showEnvironment();
            e.printStackTrace();
        }
    }

}
