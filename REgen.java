import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
/*
  Generate random regular expressions.
  Optionally, you can also generate "bad" regular expressions.

  9/17/2016, Nathan
*/

public class REgen {
    static Random rand = new Random();
    //input: max complexity, min complexity
    private static char[] test_alphabet = {'a', 'b', 'c', 'd', '1', '2', '3', '4', '.'};
    private static char[] deref_alphabet= {'*', '|', '\\','+','?','.', '(', ')'};
    private static int length;
    private static int complexity;

    //modify these if you want to specify the chance for deref and operator tokens in a string
    //set them to one to disable them (edit - can be done through arguments now)
    private static int deref_alphabet_chance = 15;
    private static int random_deref_chance = 25;
    private static int literal_no_brackets_chance = 2;
    private static int combinator_no_brackets_chance = 2;
    private static int stacked_repeat_chance = 5;
    //private static final int deref_alphabet_chance = 1;         // -> always evaluates to false
    //private static final int random_deref_chance = 1;           // -> always evaluates to false
    //private static final int literal_no_brackets_chance = 1;    // -> always evaluates to false
    //private static final int combinator_no_brackets_chance = 1; // -> always evaluates to false
    //private static final int stacked_repeat_chance = 1;         // -> always evaluates to false
    
    private static boolean valid_regex = true;
    //private static boolean stacked_repeats_allowed = true;
    public static void main(String[] args) {
	if(args.length != 3 && args.length != 7) {
	    System.err.println("err: expected three arguments but recieved " + args.length +
			       "\nusage: [-v/-i/-is/-vs (valid,invalid,invalid_nostacking," +
			       "valid_stacking)] [complexity -> int] [length -> int]");
	    System.exit(1);
	}
	try {
	    //ensure the first argument is one of these things
	    if(args[0].equals("invalid") || args[0].equals("-i"))
		valid_regex = false;
	    else if (args[0].equals("valid") || args[0].equals("-v")) {
		valid_regex = true;
		stacked_repeat_chance = 1;
	    }
	    else if (args[0].equals("valid-stacking") || args[0].equals("-vs")) {
		valid_regex = true;
	    }
	    else if (args[0].equals("invalid-stacked") || args[0].equals("-is")) {
		valid_regex = false;
		stacked_repeat_chance = 1;
	    }
	    
	    else {
		System.err.println("err: missing argument (-v/-i)\n" +
				   "usage: [-v/-i/-is/-vs (valid, invalid, invalid_nostacking, " +
				   "valid_stacking) ] [max complexity -> int]" +
				   "[min complexity -> int]");
		System.exit(1);
	    }
	    complexity = Integer.parseInt(args[1]);
	    length = Integer.parseInt(args[2]);
	    if(args.length == 7) {
		deref_alphabet_chance = Integer.parseInt(args[3]);
		random_deref_chance = Integer.parseInt(args[4]);
		literal_no_brackets_chance = Integer.parseInt(args[5]);
		combinator_no_brackets_chance = Integer.parseInt(args[6]);
	    }
	} catch (Exception e) {
	    System.err.println("err: expected argument formatted as integers\n" + 
			       "usage: [-v/-i (valid, invalid) ] [max complexity -> int]" +
			       "[min complexity -> int]");
	    System.exit(1);
	}
	
	if(valid_regex) //making a valid regex
	    printList(gen_regex_tokens(complexity, length));
	else            //making an invalid regex
	    printList(bad_regex(complexity, length));
    }

    //we need the maker to tokenize the output for this to work
    public static LinkedList<String> bad_regex(int complexity, int tokensize) {
	LinkedList<String> base_set = gen_regex_tokens(complexity, tokensize);
	LinkedList<LinkedList<String>> output_set = new LinkedList<LinkedList<String>>();
	
	if(base_set.size() ==0)
	    return base_set;

	int index;
	String s;
	//case one: unbalanced brackets
	//just pick any random token and add a bracket to either the front or the end
	index = rand.nextInt(base_set.size());
	s = base_set.get(index);
	modify_in_set("(" +s, output_set, base_set, index);
	modify_in_set(")" +s, output_set, base_set, index);
	modify_in_set(s +"(", output_set, base_set, index);
	modify_in_set(s +"(", output_set, base_set, index);

	//case two: misplaced pipe
	//try adding a pipe at the start and end of the regex,
	//and also pick random brackets, and add a pipe before/after them
	//then pick random operators (*+?|) and put a pipe before them
	//also, add in a couple of empty brackets () - which are defined to be invalid!
	front_of_set("|", output_set, base_set);
	end_of_set("|", output_set, base_set);
	if((index = base_set.indexOf("(")) != -1) modify_in_set("(|", output_set, base_set, index);
	if((index = base_set.indexOf("(")) != -1) modify_in_set("(()", output_set, base_set, index);
	if((index = base_set.indexOf(")")) != -1) modify_in_set("|)", output_set, base_set, index);
	if((index = base_set.indexOf(")")) != -1) modify_in_set("())", output_set, base_set, index);
	if((index = base_set.indexOf("*")) != -1) modify_in_set("|*", output_set, base_set, index);
	if((index = base_set.indexOf("+")) != -1) modify_in_set("|+", output_set, base_set, index);
	if((index = base_set.indexOf("?")) != -1) modify_in_set("|?", output_set, base_set, index);
	if((index = base_set.indexOf("|")) != -1) modify_in_set("||", output_set, base_set, index);
	
	//misplaced backslash at end of expression -> failure
	end_of_set("\\", output_set, base_set);
	
	//produce stacked repitition operators(if enabled)
	if(stacked_repeat_chance == 1) {
	    if((index = base_set.indexOf("*")) != -1){
		modify_in_set("**", output_set, base_set, index);
		modify_in_set("*?", output_set, base_set, index);
		modify_in_set("*+", output_set, base_set, index);
	    }
	    if((index = base_set.indexOf("?")) != -1){
		modify_in_set("?*", output_set, base_set, index);
		modify_in_set("??", output_set, base_set, index);
		modify_in_set("?+", output_set, base_set, index);
	    }
	    if((index = base_set.indexOf("+")) != -1){
		modify_in_set("+*", output_set, base_set, index);
		modify_in_set("+?", output_set, base_set, index);
		modify_in_set("++", output_set, base_set, index);
	    }
	}	
	
	return output_set.get(rand.nextInt(output_set.size()));
    }

    /*
      Grammar:
      E -> T                   expression can be a simple term, or concatenation
      E -> TE                  
      
      T -> F                   term can be a factor
      T -> F*    CLOSURE       or a factor arbitrarily many(or few) times
      T -> F?    MAYBE         or maybe a factor, but maybe not
      T -> F+    PROGRESSION   or a factor at least once
      T -> F|T   EITHER        otherwise a term could be a factor or a term
      
      F -> literal
      F -> (E)
      
      Specification:
      .          -> wildcard
      adjacency  -> concatenation
      *          -> closure
      +          -> once or more
      ?          -> zero or once
      |          -> match either
      ( )        -> context: must not be empty
      \          -> intepret as a literal
      
      precedence:
      \	        (dereference)
      ()            (brackets)
      *+?           (multipliers)
      concat        (concatenations)
      |             (selection)
      . or literal  (vocabulary)
    */
    //only difference from gen_regex is it produces a tokenized version - output should be identical
    public static LinkedList<String> gen_regex_tokens(int complexity, int tokensize) {
	String regex;
	LinkedList<String> regex_a = new LinkedList<String>();
	LinkedList<String> regex_b = new LinkedList<String>();
	LinkedList<String> output_regex = new LinkedList<String>();
	int option;
	if(complexity == 1) {
	    //a a* a+ a?
	    regex = gen_literals(tokensize);
	    //decide if we do or don't want brackets now
	    
	    option = rand.nextInt(4);

	    if(rand.nextInt(literal_no_brackets_chance) != 0) {
		output_regex.add("(");
		output_regex.add(regex);
		output_regex.add(")");
	    }
	    else output_regex.add(regex);

	    switch(option) {
	    case 0: /* do nothing here */  break;
	    case 1: output_regex.add("*"); break;
	    case 2: output_regex.add("?"); break;
	    case 3: output_regex.add("+"); break;
	    }
	    //if we're want to stack repeated multipliers, we can do so here
	    while(rand.nextInt(stacked_repeat_chance) == 1) {
		option = rand.nextInt(3);
		switch(option) {		
		case 0: output_regex.add("*"); break;
		case 1: output_regex.add("?"); break;
		case 2: output_regex.add("+"); break;
		}
	    }
	    return output_regex;
	}
	else {
	    //generate a couple of simple regexes
	    if(complexity == 2) {
		regex_a = gen_regex_tokens(1, tokensize);
		regex_b = gen_regex_tokens(1, tokensize);
	    }
	    else if (complexity == 3) {
		regex_a = gen_regex_tokens(1, tokensize);
		regex_b = gen_regex_tokens(2, tokensize);
	    }
	    else { //complexity >= 4
		int rac = rand.nextInt(complexity - 1) + 1; //will be between 1 and complexity - 1
		int inv = complexity - rac; //will also be between 1 and complexity - 1
		regex_a = gen_regex_tokens(rac, tokensize);
		regex_b = gen_regex_tokens(inv, tokensize);
	    }

	    
	    //determine if A, B are enclosed by brackets
	    if(rand.nextInt(combinator_no_brackets_chance) != 0) {
		regex_a.offerFirst("(");
		regex_a.offerLast(")");
	    }
	    if(rand.nextInt(combinator_no_brackets_chance) != 0){
		regex_b.offerFirst("(");
		regex_b.offerLast(")");
	    }
	    
	    option = rand.nextInt(4);
	    //reverse order shouldn't really be a probabilistic issue, but I might as well include it
	    if(option == 0 || option == 1) output_regex.addAll(regex_a);
	    else 		           output_regex.addAll(regex_b);

	    if(option == 0 || option == 2) output_regex.add("|");
	    
	    if(option == 0 || option == 1) output_regex.addAll(regex_b);
	    else		           output_regex.addAll(regex_a);
	    
	    return output_regex;
	}
    }
	    
		
    public static String gen_regex(int complexity, int tokensize) {
	String regex_a;
	String regex_b;
	String output_regex = "";
	int option;
	if(complexity == 1) {
	    /*
	      some possible operations that could happen here
	      a*
	      a?
	      a+
	      a
	      
	      also, (a) -> we should do roughly 75% of the time
	    */
	    regex_a = gen_literals(length);
	    option = rand.nextInt(4);
	    if(rand.nextInt(literal_no_brackets_chance) != 0)
		regex_a = "(" + regex_a + ")";
	    
	    switch(option) {
	    case 0:                 break;
	    case 1: regex_a += "*"; break;
	    case 2: regex_a += "?"; break;
	    case 3: regex_a += "+"; break;
	    }
	    while(rand.nextInt(stacked_repeat_chance) == 1) {
		option = rand.nextInt(3);
		//decide on chance of stacked repeats - going to set it to 1/5
		switch(option) {
		case 0: regex_a += "*"; break;
		case 1: regex_a += "?"; break;
		case 2: regex_a += "+"; break;
		}
	    }
	    return regex_a;
	}
	else {
	    /*
	      some possible operations we could do here (everything else is covered earlier)
	      a|b
	      ab
	      ba
	      b|a

	      optional: bracket a, bracket b - > chance based
	     */
	    
	    //generate a couple of simple regexes
	    if(complexity == 2) {
		regex_a = gen_regex(1, tokensize);
		regex_b = gen_regex(1, tokensize);
	    }
	    else if (complexity == 3) {
		regex_a = gen_regex(1, tokensize);
		regex_b = gen_regex(2, tokensize);
	    }
	    else { //complexity >= 4
		int rac = rand.nextInt(complexity - 1) + 1; //will be between 1 and complexity - 1
		int inv = complexity - rac; //will also be between 1 and complexity - 1
		regex_a = gen_regex(rac, tokensize);
		regex_b = gen_regex(inv, tokensize);
	    }
	    //determine if A, B are enclosed by brackets
	    if(rand.nextInt(combinator_no_brackets_chance) != 0)
		regex_a = "(" + regex_a + ")";
	    if(rand.nextInt(combinator_no_brackets_chance) != 0)
		regex_b = "(" + regex_b + ")";
	    option = rand.nextInt(4);
	    //reverse order shouldn't really be an issue, but might as well include it
	    switch(option) {
	    case 0: output_regex = regex_a + "|" + regex_b; break;
	    case 1: output_regex = regex_a       + regex_b; break;
	    case 2: output_regex = regex_b + "|" + regex_a; break;
	    case 3: output_regex = regex_b +       regex_a; break;
	    }
	    return output_regex;
	}
    }

    /*
      Generate a string of tokens, having size 'length'
      This includes characters from the test alphabet, sometimes having a deref prepended,
      along with characters from the deref alphabet, which always have a deref operator prepended
     */
    public static String gen_literals(int length) {
	String out = "";
	for(int i = 0; i < length; i++) {	    
	    if(rand.nextInt(deref_alphabet_chance) == 1) { //if we're using an operator and deref
		out += deref_alphabet[2]; // backslash
		out += deref_alphabet[rand.nextInt(deref_alphabet.length)];
	    }
	    else { //we're using a character:
		int sel = rand.nextInt(test_alphabet.length);
		if(rand.nextInt(random_deref_chance) == 1) //maybe give it a backslash
		    out += deref_alphabet[2];
		out += test_alphabet[sel];
	    }	    
	}
	return out;
    }

    public static void modify_in_set(String s, LinkedList<LinkedList<String>> output_set, 
				     LinkedList<String> base_set, int index) {
	output_set.add(new LinkedList<String>(base_set));
	output_set.get(output_set.size() - 1).set(index, s);
	
    }
    public static void front_of_set(String s, LinkedList<LinkedList<String>> output_set,
				    LinkedList<String> base_set) {
	output_set.add(new LinkedList<String>(base_set));
	output_set.get(output_set.size() - 1).offerFirst(s);
    }
    public static void end_of_set(String s, LinkedList<LinkedList<String>> output_set,
				  LinkedList<String> base_set) {
	output_set.add(new LinkedList<String>(base_set));
	output_set.get(output_set.size() - 1).offerLast(s);
    }
    public static void printList(LinkedList<String> l) {
	for (String str : l) System.out.print(str);
	System.out.println();
    }
}
