what this script does:
    This will basically generate a set of regular expressions according to some rules,
    and then verify that your implementation of REcompile accepts them (it should)
    Then it will generate a set of "bad" regular expressions, according to the same rules,
    and then verify that your implementation if REcompile denies them (it also should)

how to use this script:
    Initial requirements:
      -You must have compiled a java class that takes as input a single regex
      -This class must be named REcompile, and it must have a defined main
      -You should also have compiled the included REgen file
      -Ideally, this class will determine if the regex is or is not valid
      -This script will only check if your main returns 0, or does not return 0
      -Your program should return 0 for valid regular expressions
      -Your program should exit with a non-zero status otherwise
      -I'm pretty sure java crashes automatically exit with a non-zero status
      -There's an option to generate stacked multipliers. I'm not sure if the spec defines these.

Paramaters Given:
    first argument is mandatory, and is one of (-h -s -v)
      -h: help
      -v: verbose, display every given regex
      -s: silent, only provide output if a test fails

    second argument is mandatory, and is one of (+s -y -s -n)
      +s: include stacked repeat characters in the validation stage of checking (ie a**, a?+),
      	  but not in the invalidation stage of checking
      -y: same as above
      -s: do not include stacked repeat characters in the invalidation stage of checking,
      	  but not in the validation stage of checking
      -n: same as above
    
    next three arguments are also mandatory, and must all be positive integer numbers:
      LENGTH:      length of token segments in regular expression
      COMPLEXITY:  level of complexity in regex (ie amount of times *&|+ are used)
      COUNT:       number of tests to run

    There are also four optional arguments. If one of these is entered, all of them must be.
    Each of these can be disabled by setting them to one. Defaults appear in brackets.
      SPEC_CHAR_RATIO(15):  proportion of special tokens (ie *) that will appear in a literal
      RAND_SLASH_RATIO(25): proportion of characters in a literal that will have a \ prepended
      STR_BRACK_RATIO(2):   proportion of string literals that will be enclosed by brackets
      EXPR_BRACK_RATIO(2):  proportion of expressions that will be enclosed by brackets
      
    
