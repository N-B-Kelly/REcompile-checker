# REcompile-checker
REgen will generate arbitrarily complex regular expression based on a set of input arguments.

tester.sh will, given a set of arguments, test your REcompile class a bunch of times to see that it accepts what it should accept and that it also denies what it should deny.

To make proper use of this, your regex compiler should exit with a non-zero status when the regex is denied, and 0 when it is accepted. (use System.exit to stop your program).

An example of usage would be: `./tester.sh -v -y 3 3 25`

Another example usage would be: `./tester.sh -v -y 5 5 25 15 25 2 2`

If you just want to test simple cases to get an idea of what things your regex is capable of, or weed out simple errors, try `./tester.sh -v +s 1 5 50` and `./tester.sh 5 1 50`.

More detailed documentation can be found in the test_readme.txt file, or by running the command with the argument '-h'.

*WARNING:*

I make no assurances that this will 100% line up with the course specification: I'm pretty sure it does, but it's entirely possible that there's a bug somewhere, or that I missed something.

If you spot any mistakes, or can think of any error cases that aren't covered (REgen.java, look for `public static LinkedList<String> bad_regex...` - currently line 86), then just send me an email, make a pull request,  or open an issue.
