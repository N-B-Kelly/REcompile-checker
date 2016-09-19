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
    private static int length = 1;
    private static int complexity = 1;

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
    static boolean return_error_code = false;
    static boolean construct_regex_from_error_code = false;
    static int error_code = -1;
    private static int result_val = 0;
    static String usage = "usage:    [-v/-i/-is/-vs] [(complexity), (length)]\noptional: [-ec] [-fe (int)] [(deref_alph), (rand_deref), (lit_no_brackets), (comb_no_brackets)]\n  to see more details documentation, look at the readme.";

    /*
      valid arguments:
      [-v/-i/-is/-vs]            -> (valid, invalid, valid_stack, invalid_stack)
      (optional)   [-ec ]                     -> returns error code for item            not compatible with valid regex's
      (optional)   [-fe (int)]                -> constructs error based on error code   not compatible with valid regex's
      
      [(int) (int)]              -> complexity, length
      (optional)   [(int) (int) (int) (int)]  -> control character (dereferenced) chance, random backslash chance,
      chance of brackets on literals, chance on combinators  
    */
    
    public static void main(String[] args) {	
	argHandle(args);
	
	if(valid_regex) //making a valid regex
	    printList(gen_regex_tokens(complexity, length));
	else if(construct_regex_from_error_code)           //making an invalid regex
	    printList(bad_regex(complexity, length, error_code));
	else
	    printList(bad_regex(complexity, length, 0));

	//if(return_error_code)
	    System.exit(result_val);
    }


    
    //we need the maker to tokenize the output for this to work
    //take an errcode as input -> give one as output.
    //this way, we can try to reduce errors to more minimal sets (outside of this program)

    //TODO: finish this part (at some point)
    public static LinkedList<String> bad_regex_from_ec(LinkedList<String> base_set, int ercode) {// -> pretty sure I don't need to bother doing it like this
	int index = rand.nextInt(base_set.size());
	String 	s = base_set.get(index);
	result_val = ercode;
	switch(ercode) {
	case 2: base_set.set(index, "(" + s);
	case 16: base_set.add("\\"); return base_set;
	default: break;	
	}

	return null;
    }
    public static LinkedList<String> bad_regex(int complexity, int tokensize, int ercode) {
	LinkedList<String> base_set = gen_regex_tokens(complexity, tokensize);
	if(ercode > 0) {
	    LinkedList<String> br = bad_regex_from_ec(base_set, ercode);
	    if(br != null)
		return br;
	}
	    
	LinkedList<LinkedList<String>> output_set = new LinkedList<LinkedList<String>>();
	
	if(base_set.size() ==0)
	    return base_set;
	boolean valid[] = new boolean[18];
	for(int i = 0; i < 6; i++)
	    valid[i] = true;
	for(int i = 6; i < 18; i++)
	    valid[i] = false;
	int index;
	String s;
	//case one: unbalanced brackets
	//just pick any random token and add a bracket to either the front or the end
	index = rand.nextInt(base_set.size());
	s = base_set.get(index);

	//0-6 -> assured to be valid
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
	//6-18 -> probably valid, but maybe not
	if((valid[6] = (index = base_set.indexOf("(")) != -1))  modify_in_set("(|", output_set, base_set, index);
	if((valid[7] = (index = base_set.indexOf("(")) != -1)) modify_in_set("(()", output_set, base_set, index);
	if((valid[8] = (index = base_set.indexOf(")")) != -1))  modify_in_set("|)", output_set, base_set, index);
	if((valid[9] = (index = base_set.indexOf(")")) != -1)) modify_in_set("())", output_set, base_set, index);
	if((valid[10] = (index = base_set.indexOf("|")) != -1)) modify_in_set("|*", output_set, base_set, index);
	if((valid[11] = (index = base_set.indexOf("|")) != -1)) modify_in_set("|+", output_set, base_set, index);
	if((valid[12] = (index = base_set.indexOf("|")) != -1)) modify_in_set("|?", output_set, base_set, index);
	if((valid[13] = (index = base_set.indexOf("|")) != -1)) modify_in_set("||", output_set, base_set, index);
	
	end_of_set("\\", output_set, base_set); //misplaced backslash at end of expression -> failure
	valid[14] = true;
	//produce stacked repitition operators(if enabled)
	if(stacked_repeat_chance == 1) {
	    if((valid[15] = (index = base_set.indexOf("*")) != -1)){
		modify_in_set("**", output_set, base_set, index);
		modify_in_set("*?", output_set, base_set, index);
		modify_in_set("*+", output_set, base_set, index);
	    }
	    if((valid[16] = (index = base_set.indexOf("?")) != -1)){
		modify_in_set("?*", output_set, base_set, index);
		modify_in_set("??", output_set, base_set, index);
		modify_in_set("?+", output_set, base_set, index);
	    }
	    if((valid[17] = (index = base_set.indexOf("+")) != -1)){
		modify_in_set("+*", output_set, base_set, index);
		modify_in_set("+?", output_set, base_set, index);
		modify_in_set("++", output_set, base_set, index);
	    }
	}	

	int set_val = rand.nextInt(output_set.size());
	
	//IF WE WERE GIVEN A SPECIFIC ERROR TO TRY TO REPRODUCE IT
	//IF WE CAN'T, JUST SET RETURN STATUS TO 0 AND GIVE A RANDOM ERROR
	if(ercode > 1 && ercode < 20) {
	    ercode -= 2;
	    if(valid[ercode]) {
		index = 0;
		for(int i = 0; i < ercode; i++)
		    if(valid[i])
			index++;
		result_val = ercode;
		return output_set.get(index);
	    }
	    else {
		result_val = 0;
		return output_set.get(set_val);
	    }
	}

	int res = 2; //offset error code by 2, so we can return 0 and 1 without interruption
	for(int i = 0; i < set_val; i++)
	    if(valid[set_val])
		res++;
	
	result_val = res;
	
	return output_set.get(set_val);
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


    //VERY UGLY LOOKING ARGUMENT HANDLER
    private static boolean argHandle(String[] args) {
	boolean type_specified = false;
	int number_ints_filled = 0;
	int args_checked = 0;
	
	for(int i = 0; i < args.length; i++) {
	    if(!type_specified) {
		type_specified = true;
		if(args[i].equals("-v") || args[i].equals("-valid")) {
		    valid_regex = true;
		    stacked_repeat_chance = 1;
		}
		else if (args[i].equals("-i") || args[i].equals("-invalid")) {
		    valid_regex = false;
		}
		else if (args[i].equals("-vs") || args[i].equals("-valid-stacking")) {
		    valid_regex = true;
		}
		else if (args[i].equals("-is") || args[i].equals("-invalid-stacked")) {
		    valid_regex = false;
		    stacked_repeat_chance = 1;
		}
		else //failure case -> make sure we can check for this gain 
		    type_specified = false;
	    }
	    else if (!return_error_code && (args[i].equals("-ec") || args[i].equals("-return-error-code")))
		return_error_code = true;
	    else if (!construct_regex_from_error_code && (args[i].equals("-fe") || args[i].equals("-from-error"))) {
		boolean successful = false;
		if(args.length - 1 > i) { //check for overflow
		    try {
			error_code = Integer.parseInt(args[i+1]);
			successful = true;
		    } catch (Exception e) {
			successful = false;
		    }
		}
		if(!successful) {
		    System.err.println(usage);
		    System.exit(1);
		}
		construct_regex_from_error_code = true;
	    }
	    else {
		boolean successful = false;
		int res = 0;
		try {
		    res = Integer.parseInt(args[i]);
		    successful = true;
		} catch (Exception e) {
		    successful = false;
		}
		if(successful) {
		    switch(number_ints_filled) {
		    case 0:  length = res; break;
		    case 1:  complexity = res; break;
		    case 2:  deref_alphabet_chance = res; break;
		    case 3:  random_deref_chance = res; break;
		    case 4:  literal_no_brackets_chance = res; break;
		    case 5:  combinator_no_brackets_chance = res; break;
		    default: break;
		    }
		    number_ints_filled++;
		}
		else {
		    System.err.println(usage);
		    System.exit(1);
		}
	    }
	}
	
	return type_specified;
    }
}
