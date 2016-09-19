#!/bin/bash
#arguments: length, complexity, number_of_tests
#format   : int     int         int
USAGE="USAGE: [-v/-s/-h] [+/-s] [int length] [int complexity] [int number_of_tests]"
USAGE_ADV="[special char ratio] [random backslash ratio] [literal bracket ratio] [expr bracket ratio]"
HELP="run with -h command for detailed help"
RE="^[1-9]+([0-9]*)$"

LENGTH=$3
COMPLEXITY=$4
COUNT=$5

DEREF_ALPH=$6
RAND_DEREF=$7
LITERAL_NO_BRACKET=$8
LITERAL_BRACKET=$9
INVALID_ARG="-is"
INVALID_STK="-i"

if [ $1 = "-h" ] ; then 
    cat test_readme.txt
    exit 0
fi

#if REgen isn't found, we can try to compile it
if ! [ -f "REgen.class" ] ; then
    echo "REgen.class not found: trying to compile instead..."
    if ! ( [ -f "REgen.java" ] && javac REgen.java > /dev/null 2>&1 ) ; then
	echo "error: REgen.class does not seem to exist, and REgen.java either doesn't exist or is uncompilable"
	exit 1
    fi
    echo "Succesfully compiled REgen.java"
fi

#if REcompile isn't found, we can try to compile it	
if ! [ -f "REcompile.class" ] ; then
    echo "REcompile.class not found: trying to compile instead..."
    if ! ( [ -f "REcompile.java" ] && javac REcompile.java > /dev/null 2>&1 ) ; then
	echo "error: REcompile.class was not found, and REcompile.java appears to be absent or uncompilable"
	exit 1
    fi
    echo "Succesfully compiled REcompile.java"
fi


#CHECK ARGUMENTS -------------------------------------------------------------------------------------------

if [[ ! $1 = "-h" ]] && [[ ! $1 = "-v" ]] && [[ ! $1 = "-s" ]] ; then
    echo "$USAGE"
    echo "$HELP"
    exit 1
fi

if [[ ! $2 = "+s" ]] && [[ ! $2 = "-s" ]] && [[ ! $2 = "-y" ]] && [[ ! $2 = "-n" ]] ; then
    echo "$USAGE"
    echo "$HELP"
    exit 1
fi

if ! [[ $3 =~ $RE ]] ; then
    echo "argument given for [length] was missing or not positive a number" >&2;
    echo "$USAGE"
    echo "$HELP"
    exit 1
fi

if ! [[ $4 =~ $RE ]] ; then
    echo "argument given for [complexity] was missing or not a positive number" >&2;
    echo "$USAGE"
    echo "$HELP"
    exit 1
fi

if ! [[ $5 =~ $RE ]] ; then
    echo "argument given for [number_of_tests] was missing or not a positive number" >&2;
    echo "$USAGE"
    echo "$HELP"
    exit 1
fi

#FIRST SET OF ARGUMENTS CHECKED-----------------------------------------------------------------------------

#CASE: five args -> 3 checker args
if [ $# -eq 5 ] ; then
    COUNTER=0
    until [ $COUNTER -eq $COUNT ]; do
	let COUNTER+=1
	#GENERATE REGEX
	if [[ $2 = "+s" ]] || [[ $2 = "-y" ]] ; then
	    REGEX="$(java REgen -vs $LENGTH $COMPLEXITY 15 25 2 2)"
	else
	    REGEX="$(java REgen -v $LENGTH $COMPLEXITY 15 25 2 2)"
	fi
	
	#if verbose, echo regular expression
	if [ $1 = "-v" ] ; then
	    echo "$REGEX"
	fi
	
	#CHECK IF IT WORKED
	if ! java REcompile "$REGEX" > /dev/null 2>&1 ; then
	    #IF IT FAILED, OUTPUT THE OFFENDING REGEX
	    echo "test number $COUNTER failed."
	    echo "regular expression should have been accepted: $REGEX"
	    exit 1
	fi
    done
    
    if [ $1 = "-v" ] ; then
	echo "all valid expressions were accepted"
    fi

    #now we check to see that all rejections occur as they should
    COUNTER2=0
    until [ $COUNTER2 -eq $COUNT ]; do
	let COUNTER2+=1
	#GENERATE REGEX
	if [[ $2 = "+s" ]] || [[ $2 = "-y" ]] ; then
	    REGEX="$(java REgen $INVALID_STK $LENGTH $COMPLEXITY 15 25 2 2)"
	else
	    REGEX="$(java REgen $INVALID_ARG $LENGTH $COMPLEXITY 15 25 2 2)"
	fi
	#if verbose, echo regular expression
	if [ $1 = "-v" ] ; then
	    echo "$REGEX"
	fi
	
	#CHECK IF IT WORKED
	if java REcompile "$REGEX" > /dev/null 2>&1 ; then
	    #IF IT FAILED, OUTPUT THE OFFENDING REGEX
	    echo "test number $COUNTER2 failed."
	    echo "regular expression should have been denied: $REGEX"
	    exit 1
	fi
    done
 
    if [ $1 = "-v" ] ; then
	echo "all invalid expressions were rejected"
    fi

    echo "all tests passed"
    exit 0
fi


#CASE: EIGHT ARGUMENTS - MORE ARG CHECKING-------------------------------------------------------------------

if ! [[ $6 =~ $RE ]] ; then
    echo "argument given for [special char ratio] was missing or not positive a number" >&2;
    echo "$USAGE"
    echo "$USAGE_ADV"
    echo "$HELP"
    exit 1
fi

if ! [[ $7 =~ $RE ]] ; then
    echo "argument given for [random backslash ratio] was missing or not positive a number" >&2;
    echo "$USAGE"
    echo "$USAGE_ADV"
    echo "$HELP"
    exit 1
fi

if ! [[ $8 =~ $RE ]] ; then
    echo "argument given for [literal bracket ratio] was missing or not a positive number" >&2;
    echo "$USAGE"
    echo "$USAGE_ADV"
    echo "$HELP"
    exit 1
fi

if ! [[ $9 =~ $RE ]] ; then
    echo "argument given for [expr bracket ratio] was missing or not a positive number" >&2;
    echo "$USAGE"
    echo "$USAGE_ADV"
    echo "$HELP"
    exit 1
fi

#END ARG CHECKING 2 -----------------------------------------------------------------------------------------

COUNTER=0
until [ $COUNTER -eq $COUNT ]; do
    #GENERATE REGEX
    if [[ $2 = "+s" ]] || [[ $2 = "-y" ]] ; the
	REGEX="$(java REgen -vs $LENGTH $COMPLEXITY $6 $7 $8 $9)"
    else
	REGEX="$(java REgen -v $LENGTH $COMPLEXITY $6 $7 $8 $9)"
    fi


    
    #if verbose, echo regular expression
    if [ $1 = "-v" ] ; then
	echo "$REGEX"
    fi
    
    #CHECK IF IT WORKED
    if ! java REcompile "$REGEX" > /dev/null 2>&1 ; then
	#IF IT FAILED, OUTPUT THE OFFENDING REGEX
	echo "test number $COUNTER failed."
	echo "regular expression: $REGEX"
	exit 1
    fi
    let COUNTER+=1
done

if [ $1 = "-v" ] ; then
    echo "all valid expressions were accepted"
fi

#now we check to see that all rejections occur as they should
COUNTER2=0
until [ $COUNTER2 -eq $COUNT ]; do
    #GENERATE REGEX
    if [[ $2 = "+s" ]] || [[ $2 = "-y" ]] ; then
	REGEX="$(java REgen $INVALID_STK $LENGTH $COMPLEXITY $6 $7 $8 $9)"
    else
	REGEX="$(java REgen $INVALID_ARG $LENGTH $COMPLEXITY $6 $7 $8 $9)"
    fi
    #if verbose, echo regular expression
    if [ $1 = "-v" ] ; then
	echo "$REGEX"
    fi
    
    #CHECK IF IT WORKED
    if java REcompile "$REGEX" > /dev/null 2>&1 ; then
	#IF IT FAILED, OUTPUT THE OFFENDING REGEX
	echo "test number $COUNTER2 failed."
	echo "regular expression should have been denied: $REGEX"
	exit 1
    fi
    let COUNTER2+=1
done

if [ $1 = "-v" ] ; then
    echo "all invalid expressions were rejected"
fi

echo "all tests passed"
exit 0
