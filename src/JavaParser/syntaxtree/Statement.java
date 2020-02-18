//
// Generated by JTB 1.3.2
//

package JavaParser.syntaxtree;

/**
 * Grammar production:
 * f0 -> Block()
 *       | AssignmentStatement()
 *       | ArrayAssignmentStatement()
 *       | IfStatement()
 *       | WhileStatement()
 *       | PrintStatement()
 */
public class Statement implements Node {
   public NodeChoice f0;

   public Statement(NodeChoice n0) {
      f0 = n0;
   }

   public void accept(JavaParser.visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(JavaParser.visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(JavaParser.visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(JavaParser.visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
}

