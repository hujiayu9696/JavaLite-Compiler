import JavaToVapor.J2VVisitor;
import VaporMToMIPS.VM2M;
import VaporToVaporM.V2VM;
import JavaParser.MiniJavaParser;
import JavaParser.syntaxtree.Node;
import TypeChecker.TypeCheckVisitor;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;

public class minijava {

    public static void main(String[] args) {
        try {
            if (args.length != 2)
            {
                System.out.println("Argument Error.");
                System.out.println("Usage: minijava sourcefile outputfile");
                return;
            }

            // TypeCheck
            InputStream in = new FileInputStream(args[0]);
            Node root = (new MiniJavaParser(in)).Goal();
            root.accept(new TypeCheckVisitor());

            // To Vapor
            J2VVisitor j2VVisitor = new J2VVisitor();
            root.accept(j2VVisitor);
            String vaporCode = j2VVisitor.vaporCode.toString();

            // To VaporM
            String vaporMCode = V2VM.toVaporM(new ByteArrayInputStream(vaporCode.getBytes()));

            // To MIPS
            String MIPSCode = VM2M.toMIPS(new ByteArrayInputStream(vaporMCode.getBytes()));

            // Output
            PrintWriter out = new PrintWriter(args[1]);
            out.print(MIPSCode);
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
