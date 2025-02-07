package edu.illinois.cs.analysis;

import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CodeModifier extends VoidVisitorAdapter
{

	/**
	 * This visit function will be automatically applied to all binary
	 * expressions in the given Java file
	 */
	@Override
	public void visit(BinaryExpr n, Object arg) {
		super.visit(n, arg);
		// TODO: complete this visit function to automatically switch the two
		// operands for all null checks specified in the homework assignment
		// https://stackoverflow.com/questions/68871869/javaparser-removing-an-operator-from-the-expression-at-the-ast-level-how-to-re
		// https://github.com/javaparser/javaparser/issues/1622
		if (n.getOperator() == BinaryExpr.Operator.EQUALS || n.getOperator() == BinaryExpr.Operator.NOT_EQUALS) {
			if (n.getLeft() instanceof  NullLiteralExpr || n.getRight() instanceof NullLiteralExpr) {
				Expression temp = n.getRight();
				n.setRight(n.getLeft());
				n.setLeft(temp);
			}
		}
	}

	// The following three visit functions are used to remove all comments to
	// facilicate the comparison with the expected version; please do not change
	@Override
	public void visit(LineComment n, Object arg) {
		n.remove();
	}

	@Override
	public void visit(BlockComment n, Object arg) {
		n.remove();
	}

	@Override
	public void visit(JavadocComment n, Object arg) {
		n.remove();
	}

}
