package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, values for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 * 
	 *               ’Twas brillig, and the slithy toves Did gyre and gimble in the
	 *               wabe: All mimsy were the borogoves, And the mome raths
	 *               outgrabe.
	 * 
	 *               “Beware the Jabberwock, my son! The jaws that bite, the claws
	 *               that catch! Beware the Jubjub bird, and shun The frumious
	 *               Bandersnatch!”
	 * 
	 *               He took his vorpal sword in hand; Long time the manxome foe he
	 *               sought— So rested he by the Tumtum tree And stood awhile in
	 *               thought.
	 * 
	 *               And, as in uffish thought he stood, The Jabberwock, with eyes
	 *               of flame, Came whiffling through the tulgey wood, And burbled
	 *               as it came!
	 * 
	 *               One, two! One, two! And through and through The vorpal blade
	 *               went snicker-snack! He left it dead, and with its head He went
	 *               galumphing back.
	 * 
	 *               “And hast thou slain the Jabberwock? Come to my arms, my
	 *               beamish boy! O frabjous day! Callooh! Callay!” He chortled in
	 *               his joy.
	 * 
	 *               ’Twas brillig, and the slithy toves Did gyre and gimble in the
	 *               wabe: All mimsy were the borogoves, And the mome raths
	 *               outgrabe.
	 * 
	 * 
	 * 
	 * 
	 *               Nelson Vargas RUID: 184-00-3905 3/7/19 "The world I love, the
	 *               tears I drop, to be part of the wave can't stop. Ever wonder if
	 *               it's all for you?"
	 * 
	 *               Not gonna lie, this project was rough. I'm proud of myself for
	 *               pulling through.
	 * 
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		/**
		 * DO NOT create new vars and arrays - they are already created before being
		 * sent in to this method - you just need to fill them in.
		 **/

		StringTokenizer stringToken = new StringTokenizer(expr, delims, true);
		ArrayList<String> arrayCheckForDuplicates = new ArrayList<String>();
		String[] potentialArrayStringTokens = new String[stringToken.countTokens()];
		int num = stringToken.countTokens();

		// Places tokens in arrays to check for Array objects
		for (int i = 0; i < num; i++) {
			potentialArrayStringTokens[i] = stringToken.nextToken();
		}

		// Does the check for Array objects
		for (int j = 0; j < potentialArrayStringTokens.length - 1; j++) {
			if (potentialArrayStringTokens[j + 1].equals("[")) {
				if (!(arrayCheckForDuplicates.contains(potentialArrayStringTokens[j]))) {
					arrayCheckForDuplicates.add(potentialArrayStringTokens[j]);
					arrays.add(new Array(potentialArrayStringTokens[j]));
					// System.out.println("This was added as an ARRAY : " +
					// potentialArrayStringTokens[j]);
				}
			}
		}
		// Stores everything else as Variable objects and makes sure not to count Array
		// objects
		StringTokenizer stringTokenForVar = new StringTokenizer(expr, delims, false);
		ArrayList<String> variableCheckForDuplicates = new ArrayList<String>();

		while (stringTokenForVar.hasMoreTokens()) {
			String stToken = stringTokenForVar.nextToken();
			if (!(variableCheckForDuplicates.contains(stToken))) {
				if (!(arrayCheckForDuplicates.contains(stToken))) {
					if (!(isNumeric(stToken))) {
						variableCheckForDuplicates.add(stToken);
						vars.add(new Variable(stToken));
						// System.out.println("This was added as a VARIABLE : " + stToken);
					}
				}
			}
		}
		// System.out.println("---------------------------");
		// System.out.println("END OF MAKE VARIABLE LISTS");
		// System.out.println("---------------------------");

	}

	private static boolean isNumeric(String token) {
		return token.matches("-?\\d+(\\.\\d+)?");
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars   The variables array list, with values for all variables in the
	 *               expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

		String expression = expr.replace(" ", ""); // remove the spaces
		String newString = ""; // will contain the string with variables replaced with their values
		Stack<String> operators = new Stack<String>();
		Stack<Float> values = new Stack<Float>();

		// checks for arrays in the expression and adds them in an arraylist to keep
		// reference
		ArrayList<String> arrayObjects = new ArrayList<String>();
		for (Array a : arrays) {
			// System.out.println(a);
			arrayObjects.add(a.name);
		}

		// Populates an array with all the tokens of the expression
		StringTokenizer populateArray = new StringTokenizer(expression, delims, true);

		int exprLength = populateArray.countTokens();
		String[] exprArray = new String[exprLength];

		// replaces the variable name with the value (as a string)
		for (int i = 0; i < exprLength; i++) {
			String replacedString = populateArray.nextToken();
			exprArray[i] = replacedString;
			for (Variable m : vars) {
				if (m.name.equals(exprArray[i])) {
					exprArray[i] = Integer.toString(m.value);
				}
			}
		}

		// Creates a new String with the replaced variable values
		for (int m = 0; m < exprLength; m++) {
			newString += exprArray[m];
		}

		// System.out.println("******************************");
		// System.out.println("EXPRESSION TO BE EVALUATED: " + newString);
		// System.out.println("******************************");

		for (int j = 0; j < exprArray.length; j++) {
			switch (exprArray[j]) {

			case "(":
				// System.out.println("Recursion detected!!!");
				String stringToRecurse = findSubExpression(exprArray, j);
				// System.out.println("SUBEXPRESSION BEING RECURSED AND EVALUATED " +
				// stringToRecurse);
				values.push(evaluate(stringToRecurse, vars, arrays));
				j += skipElementFinder(stringToRecurse);
				break;

			case "+":
				// System.out.println("+ has been pushed on to the operators stack");
				break;

			case "-":
				String nextOperand = exprArray[j + 1];
				// System.out.println("+ has been pushed on to the operators stack (adding a
				// negative to maintain precedence)");
				operators.push("+");

				if (!nextOperand.equals("(") && !arrayObjects.contains(nextOperand)) {
					float negative = Float.parseFloat(nextOperand);
					negative = negative * (-1);
					// System.out.println(negative + " has been pushed on to the values stack");
					values.push(negative);
					j++;
				}
				if (nextOperand.equals("(")) {
					String minusValue = findSubExpression(exprArray, j + 1);
					j += skipElementFinder(minusValue);
					values.push(evaluate(minusValue, vars, arrays) * -1);

					// System.out.println("j has been skipped " + j + " times");
				}
				if (arrayObjects.contains(nextOperand)) {
					float bracketNegative = 0;

					String bracketMinusExpression = findBracketSubExpression(exprArray, j + 2);
					int indexMinusBracket = (int) evaluate(bracketMinusExpression, vars, arrays);
					for (Array g : arrays) {
						if (g.name.equals(nextOperand)) {
							bracketNegative = g.values[indexMinusBracket];
						}
					}
					float difference = bracketNegative * -1;
					values.push(difference);
					// System.out.println(difference+ " has been pooooosehed");
					j += skipElementFinder(bracketMinusExpression) + 1;

				}
				break;

			case "/":
				String divisorCheck = exprArray[j + 1];
				if (!divisorCheck.equals("(") && !arrayObjects.contains(divisorCheck)) {
					float dividend = values.pop();
					float divisor = Float.parseFloat(exprArray[j + 1]);
					values.push(dividend / divisor);
					j++;
				}
				if (divisorCheck.equals("(")) {
					String divisionValue = findSubExpression(exprArray, j + 1);
					j += skipElementFinder(divisionValue);
					float dividendRecursive = values.pop();
					float divisorRecursive = evaluate(divisionValue, vars, arrays);
					values.push(dividendRecursive / divisorRecursive);
//    				System.out.println(dividendRecursive +" has been DIVIDED by " + divisorRecursive + " (division automatically grabs the next element) and...");
//        			System.out.println((dividendRecursive / divisorRecursive) + " has been pushed on to  the values stack");

				}
				if (arrayObjects.contains(divisorCheck)) {
					float dividendBracket = values.pop();
					float divisorBracket = 0;
					float quotient = 0;
					String divisionBracketExpression = findBracketSubExpression(exprArray, j + 2);
					int indexForDivisionBracket = (int) evaluate(divisionBracketExpression, vars, arrays);

					for (Array s : arrays) {
						if (divisorCheck.equals(s.name)) {
							divisorBracket = (float) s.values[indexForDivisionBracket];

						}
					}
					quotient = dividendBracket / divisorBracket;
					values.push(quotient);
					j += skipElementFinder(divisionBracketExpression) + 1;

				}
				break;

			case "*":
				// System.out.println("Tripwire tripped");
				String nextTerm = exprArray[j + 1];
				if (!nextTerm.equals("(") && !arrayObjects.contains(nextTerm)) {
					float multiplicand = values.pop();
					float multiplier = Float.parseFloat(exprArray[j + 1]);
					// System.out.println(multiplicand + " has been MULTIPLIED by " + multiplier + "
					// (multiplying grabs next element automatically) and...");
					values.push(multiplicand * multiplier);
					// System.out.println(multiplicand * multiplier + " has been pushed on to the
					// values stack");
					j++;
				}
				if (nextTerm.equals("(")) {
					String multiplyTerm = findSubExpression(exprArray, j + 1);
					j += skipElementFinder(multiplyTerm);
					float multiplicandRecursive = values.pop();
					float multiplierRecursive = evaluate(multiplyTerm, vars, arrays);
					values.push(multiplicandRecursive * multiplierRecursive);
//        			System.out.println(multiplicandRecursive + " has been MULTIPLIED by " + multiplierRecursive + " (multiplying grabs next element automatically) and...");
//        			System.out.println(multiplicandRecursive * multiplierRecursive + " has been pushed on to the values stack");

				}
				if (arrayObjects.contains(nextTerm)) {
					// System.out.println("Array detected! FOR MULTIPLICATION");
					String bracketExpression = findBracketSubExpression(exprArray, j + 2);
					// System.out.println("We're going to be multiplying this " +
					// bracketExpression);
					float bracketMultiplicand = values.pop();
					// System.out.println(bracketMultiplicand +" has been popped");
					float bracketMultiplier = 0;
					float product = 0;
					// System.out.println(exprArray[j]);
					int indexForArray = (int) evaluate(bracketExpression, vars, arrays);
					for (Array y : arrays) {
						if (nextTerm.equals(y.name)) {
							bracketMultiplier = (float) y.values[indexForArray];
//    						System.out.println("////////////////////\\\\\\\\\\\\\\\\\\\\");
//    						System.out.println("WE'RE MULTIPLYING BY " 	+bracketMultiplier);

						}
					}
					product = bracketMultiplicand * bracketMultiplier;
					values.push(product);
					j += skipElementFinder(bracketExpression) + 1;

				}
				break;

			case ")":
				break;

			case "]":
				break;

			default:
				if (!arrayObjects.contains(exprArray[j])) {
					float numberValue = Float.parseFloat(exprArray[j]);
					// System.out.println(numberValue + " has been pushed on to the values stackd");
					values.push(numberValue);
					break;

				} else {

					// System.out.println(" regularArray detected!");
					String bracketExpression = findBracketSubExpression(exprArray, j + 1);

					int indexForArray = (int) evaluate(bracketExpression, vars, arrays);
					// System.out.println("index is " + indexForArray);
					float valueToPush = 0;

					for (Array x : arrays) {
						if (exprArray[j].equals(x.name)) {
							valueToPush = (float) x.values[indexForArray];

						}
					}

					j += skipElementFinder(bracketExpression) + 1;
					// System.out.println("term directly after the recursive call is" +
					// exprArray[j]);
					values.push(valueToPush);
					// System.out.println(valueToPush + " s just been pushed on to the values
					// stack");
					break;
				}

			}

		}

		while (!operators.isEmpty()) {
			String op = operators.pop();
			switch (op) {
			case "+":
				float number1 = values.pop();
				float number2 = values.pop();
				values.push(number1 + number2);
				break;

			default:
				break;

			}

		}

		float finalAnswer = values.pop();
//    	System.out.println("---------------------------");
//    	System.out.println("END OF EVALUATE <---> FINAL ANSWER: " + finalAnswer);
//    	System.out.println("---------------------------");
//    	System.out.println("Is there stuff left in the values stack?" + values.isEmpty());
		while (!values.isEmpty()) {
			finalAnswer += values.pop();
		}
		return finalAnswer;
	}
	// if expression sent was: (a+b) returns the sum of a+b

	private static String findSubExpression(String[] exprString, int startingIndex) {
		String subExpression = "";
		int openParenCheck = 1;
		startingIndex++;

		while (openParenCheck > 0 && startingIndex < exprString.length) {
			if (exprString[startingIndex].equals("(")) {
				openParenCheck++;
			}
			if (exprString[startingIndex].equals(")")) {
				openParenCheck--;
			}
			if (!(openParenCheck == 0)) {
				subExpression += exprString[startingIndex];
			}
			startingIndex++;
		}
		// System.out.println("SubExpression = " + subExpression);
		return subExpression;
	}

	private static String findBracketSubExpression(String[] exprString, int startingIndex) {
		String expressionInBrackets = "";
		int openBracketCheck = 1;
		startingIndex++;

		while (openBracketCheck > 0 && startingIndex < exprString.length) {
			if (exprString[startingIndex].equals("[")) {
				openBracketCheck++;
			}
			if (exprString[startingIndex].equals("]")) {
				openBracketCheck--;
			}
			if (!(openBracketCheck == 0)) {
				expressionInBrackets += exprString[startingIndex];
			}
			startingIndex++;
		}
		// System.out.println("Expression in brackets " + expressionInBrackets);
		return expressionInBrackets;
	}

	private static int skipElementFinder(String stringForTokenCount) {
		StringTokenizer skipTok = new StringTokenizer(stringForTokenCount, delims, true);
		int count = 0;
		while (skipTok.hasMoreTokens()) {
			skipTok.nextToken();
			count++;
		}

		return count + 1;
	}
}
