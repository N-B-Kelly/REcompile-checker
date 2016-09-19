# REcompile-checker
REgen will generate arbitrarily complex regular expression based on a set of input arguments.

tester.sh will, given a set of arguments, test your REcompile class a bunch of times to see that it accepts what it should accept and that it also denies what it should deny.

To make proper use of this, your regex compiler should exit with a non-zero status when the regex is denied, and 0 when it is accepted. (use System.exit to stop your program).

An example of usage would be: ./tester.sh -v -y 3 3 25

Another example usage would be: ./tester.sh -v -y 5 5 25 15 25 2 2


More detailed documentation can be found in the test_readme.txt file, or by running the command with the argument '-h'.