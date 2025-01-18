package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import java.util.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import java.util.List;
import java.util.stream.Collectors;

public class MutationOperators {

    public static void AMC(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldDeclaration fd, Void arg) {
                super.visit(fd, arg);

                // حذف مودیفایرهای دسترسی (public, private, protected)
                fd.getModifiers().removeIf(modifier ->
                        modifier.getKeyword() == Modifier.Keyword.PUBLIC ||
                                modifier.getKeyword() == Modifier.Keyword.PRIVATE ||
                                modifier.getKeyword() == Modifier.Keyword.PROTECTED
                );

                // اضافه کردن مودیفایر public به‌جای مودیفایر حذف‌شده
                fd.addModifier(Modifier.Keyword.PUBLIC);

                // چاپ تغییرات برای اشکال‌زدایی
                System.out.println("Access modifier changed to public for field: " + fd);
            }
        }, null);
    }
    public static void IOD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);

                // List to keep track of methods to remove
                List<MethodDeclaration> methodsToRemove = new ArrayList<>();

                // Iterate over all methods in the current class
                for (MethodDeclaration method : n.getMethods()) {
                    // Check if the method is annotated with @Override
                    if (method.isAnnotationPresent(Override.class)) {
                        methodsToRemove.add(method);
                    }
                }

                // Remove only the methods marked with @Override
                methodsToRemove.forEach(n::remove);
            }
        }, null);
    }
    public static void IOP(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ConstructorDeclaration constructor, Void arg) {
                super.visit(constructor, arg);

                // بررسی اینکه آیا سازنده حاوی دستورات است
                if (constructor.getBody().getStatements().isEmpty()) return;

                List<Statement> statements = constructor.getBody().getStatements();
                int superCallIndex = -1;

                // پیدا کردن موقعیت فراخوانی super()
                for (int i = 0; i < statements.size(); i++) {
                    Statement stmt = statements.get(i);
                    if (stmt.isExplicitConstructorInvocationStmt()) {
                        ExplicitConstructorInvocationStmt explicitStmt = stmt.asExplicitConstructorInvocationStmt();
                        // بررسی اینکه آیا دستور شامل کلمه کلیدی super است
                        if (explicitStmt.toString().startsWith("super(")) {
                            superCallIndex = i;
                            break;
                        }
                    }
                }

                // اگر فراخوانی super() وجود داشت، موقعیت آن را تغییر دهیم
                if (superCallIndex != -1) {
                    Statement superCall = statements.get(superCallIndex);
                    statements.remove(superCallIndex);

                    // انتقال فراخوانی super() به انتها
                    if (statements.size() > 0) {
                        constructor.getBody().addStatement(statements.size(), superCall);
                    } else {
                        constructor.getBody().addStatement(superCall);
                    }
                }
            }
        }, null);
    }
    public static void IOR(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                super.visit(clazz, arg);

                // بررسی اینکه آیا این کلاس پدر است
                if (!clazz.isInterface() && !clazz.isAnnotationPresent(Override.class)) {
                    clazz.getMethods().forEach(method -> {
                        // اگر متدی در فرزند اورراید شده باشد
                        boolean isOverridden = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                                .filter(childClazz -> !childClazz.equals(clazz)) // بررسی کلاس‌های فرزند
                                .flatMap(childClazz -> childClazz.getMethods().stream())
                                .anyMatch(childMethod -> childMethod.getNameAsString().equals(method.getNameAsString()));

                        if (isOverridden) {
                            String originalName = method.getNameAsString();
                            String updatedName = "p" + originalName.substring(0, 1).toUpperCase() + originalName.substring(1);

                            // تغییر نام متد در کلاس پدر
                            method.setName(updatedName);

                            // تغییر ارجاعات به این متد در سایر بخش‌ها
                            cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
                                if (methodCall.getNameAsString().equals(originalName) &&
                                        methodCall.getScope().isPresent() &&
                                        methodCall.getScope().get().toString().equals("super")) {
                                    methodCall.setName(updatedName);
                                }
                            });
                        }
                    });
                }
            }
        }, null);
    }
    public static void ISI(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);

                // بررسی وجود بدنه برای متد
                if (n.getBody().isEmpty()) {
                    return;
                }

                // بازدید از تمام متغیرها و متدهای داخل بدنه متد
                n.getBody().get().accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(NameExpr expr, Void arg) {
                        super.visit(expr, arg);

                        // بررسی اینکه متغیر یا متد در کلاس والد تعریف شده است
                        if (isDefinedInAncestorClass(expr, cu)) { // از cu به جای compilationUnit استفاده می‌کنیم
                            // اضافه کردن super به متغیر/متد
                            FieldAccessExpr superExpr = new FieldAccessExpr(new SuperExpr(), expr.getNameAsString());
                            expr.replace(superExpr);
                            System.out.println("Inserted 'super' keyword for: " + expr.getNameAsString());
                        }
                    }
                }, null);
            }
        }, null);
    }
    private static boolean isDefinedInAncestorClass(NameExpr expr, CompilationUnit cu) {
        ClassOrInterfaceDeclaration currentClass = findContainingClass(expr);
        if (currentClass == null) {
            return false; // اگر کلاس فعلی پیدا نشد
        }

        Optional<ClassOrInterfaceDeclaration> parentClass = findParentClass(currentClass, cu);
        if (parentClass.isEmpty()) {
            return false; // اگر کلاس والد وجود نداشت
        }

        String name = expr.getNameAsString();
        ClassOrInterfaceDeclaration parent = parentClass.get();

        boolean isFieldInParent = parent.getFields().stream()
                .anyMatch(field -> field.getVariables().stream()
                        .anyMatch(variable -> variable.getNameAsString().equals(name)));

        boolean isMethodInParent = parent.getMethods().stream()
                .anyMatch(method -> method.getNameAsString().equals(name));

        return isFieldInParent || isMethodInParent;
    }
    private static Optional<ClassOrInterfaceDeclaration> findParentClass(ClassOrInterfaceDeclaration currentClass, CompilationUnit cu) {
        if (!currentClass.getExtendedTypes().isEmpty()) {
            String parentName = currentClass.getExtendedTypes().get(0).getNameAsString();

            return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(cls -> cls.getNameAsString().equals(parentName))
                    .findFirst();
        }
        return Optional.empty();
    }
    private static ClassOrInterfaceDeclaration findContainingClass(NameExpr expr) {
        return expr.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
    }
    public static void ISD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(SuperExpr n, Void arg) {
                super.visit(n, arg);
                // Remove the super keyword expression
                n.remove();
                System.out.println("Deleted 'super' keyword: " + n);
            }
        }, null);
    }
    public static void IPC(CompilationUnit cu) {
        // Create a list to store nodes to be removed
        List<ExplicitConstructorInvocationStmt> nodesToRemove = new ArrayList<>();

        // First pass: Collect all super() calls to be removed
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
                super.visit(n, arg);
                // Check if the statement is a super() call (i.e., not a this() call)
                if (!n.isThis()) {
                    nodesToRemove.add(n);
                }
            }
        }, null);

        // Second pass: Remove the collected super() calls
        for (ExplicitConstructorInvocationStmt node : nodesToRemove) {
            node.remove();
            System.out.println("Deleted super constructor call: " + node);
        }
    }
    public static void PNC(CompilationUnit cu) {
        Map<String, List<String>> classHierarchy = new HashMap<>();

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                if (n.getExtendedTypes().isNonEmpty()) {
                    String parentClass = n.getExtendedTypes().get(0).getNameAsString();
                    String childClass = n.getNameAsString();
                    classHierarchy.putIfAbsent(parentClass, new ArrayList<>());
                    classHierarchy.get(parentClass).add(childClass);
                }
            }
        }, null);

        // Second pass: Modify object creation expressions
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ObjectCreationExpr n, Void arg) {
                super.visit(n, arg);

                // Get the type of the object being created
                String originalType = n.getType().asString();

                // Check if the type has any known child classes
                if (classHierarchy.containsKey(originalType)) {
                    List<String> childClasses = classHierarchy.get(originalType);
                    if (!childClasses.isEmpty()) {
                        // Select a random child class
                        String newChildType = childClasses.get((int) (Math.random() * childClasses.size()));

                        // Replace the type of the object being instantiated
                        n.setType(newChildType);

                        // Debug output
                        System.out.println("Replaced instantiation of " + originalType + " with " + newChildType + " in: " + n);
                    }
                }
            }
        }, null);
    }
    public static void PMD(CompilationUnit cu) {
        // To store mappings of variable names to their assigned object types
        final Map<String, String> variableToTypeMap = new HashMap<>();

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator variableDeclarator, Void arg) {
                super.visit(variableDeclarator, arg);

                // Handle inline declaration and initialization
                if (variableDeclarator.getInitializer().isPresent() &&
                        variableDeclarator.getInitializer().get() instanceof ObjectCreationExpr) {

                    ObjectCreationExpr initializer = (ObjectCreationExpr) variableDeclarator.getInitializer().get();
                    String actualType = initializer.getType().asString();

                    // Update the declaration type to the actual object type
                    variableDeclarator.setType(actualType);
                    variableToTypeMap.put(variableDeclarator.getNameAsString(), actualType);

                    // Debug output
                    System.out.println("Changed declaration type of variable '"
                            + variableDeclarator.getNameAsString()
                            + "' to '" + actualType + "'");
                }
            }

            @Override
            public void visit(AssignExpr assignExpr, Void arg) {
                super.visit(assignExpr, arg);

                // Handle separate declaration and assignment
                if (assignExpr.getTarget() instanceof NameExpr &&
                        assignExpr.getValue() instanceof ObjectCreationExpr) {

                    NameExpr target = (NameExpr) assignExpr.getTarget();
                    ObjectCreationExpr initializer = (ObjectCreationExpr) assignExpr.getValue();
                    String actualType = initializer.getType().asString();

                    // Find the corresponding variable declaration
                    cu.findAll(VariableDeclarator.class).stream()
                            .filter(vd -> vd.getNameAsString().equals(target.getNameAsString()))
                            .findFirst()
                            .ifPresent(vd -> {
                                vd.setType(actualType); // Update the type
                                variableToTypeMap.put(target.getNameAsString(), actualType);

                                // Debug output
                                System.out.println("Changed declaration type of variable '"
                                        + vd.getNameAsString()
                                        + "' to '" + actualType + "'");
                            });
                }
            }
        }, null);
    }
    public static void PPD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration methodDeclaration, Void arg) {
                super.visit(methodDeclaration, arg);

                // Iterate over all parameters of the method
                methodDeclaration.getParameters().forEach(parameter -> {
                    String originalType = parameter.getType().asString();

                    // Find child classes for the parameter type
                    List<String> childClasses = findChildClasses(cu, originalType);

                    if (!childClasses.isEmpty()) {
                        // Pick a random child class
                        String randomChild = childClasses.get((int) (Math.random() * childClasses.size()));

                        // Update the parameter type
                        parameter.setType(randomChild);

                        // Debug output
                        System.out.println("Changed parameter type of '" + parameter.getNameAsString()
                                + "' from '" + originalType + "' to '" + randomChild + "'");
                    }
                });
            }
        }, null);
    }
    // Helper method to find child classes of a given class type within the CompilationUnit
    private static List<String> findChildClasses(CompilationUnit cu, String parentType) {
        List<String> childClasses = new ArrayList<>();

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
                super.visit(classDeclaration, arg);

                // Check if the class extends the given parent type
                classDeclaration.getExtendedTypes().forEach(extendedType -> {
                    if (extendedType.getNameAsString().equals(parentType)) {
                        childClasses.add(classDeclaration.getNameAsString());
                    }
                });
            }
        }, null);

        return childClasses;
    }
    // PCI: Type Cast Operator Insertion
    public static void PCI(CompilationUnit cu) {
        Map<String, String> parentChildMap = new HashMap<>();

        // Step 1: Collect variables declared with parent types but initialized with child types
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator n, Void arg) {
                super.visit(n, arg);
                if (n.getInitializer().isPresent()) {
                    Expression initializer = n.getInitializer().get();
                    if (initializer instanceof ObjectCreationExpr) {
                        ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) initializer;
                        String parentType = n.getTypeAsString();
                        String childType = objectCreationExpr.getTypeAsString();
                        if (!parentType.equals(childType)) {
                            parentChildMap.put(n.getNameAsString(), parentType);
                        }
                    }
                }
            }
        }, null);

        // Step 2: Insert type casting when the variable is used
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);
                n.getScope().ifPresent(scope -> {
                    if (scope instanceof NameExpr) {
                        NameExpr nameExpr = (NameExpr) scope;
                        String varName = nameExpr.getNameAsString();
                        if (parentChildMap.containsKey(varName)) {
                            String parentType = parentChildMap.get(varName);
                            CastExpr castExpr = new CastExpr(new ClassOrInterfaceType(null, parentType), nameExpr.clone());
                            n.setScope(castExpr);
                        }
                    }
                });
            }

            @Override
            public void visit(NameExpr n, Void arg) {
                super.visit(n, arg);
                String varName = n.getNameAsString();
                if (parentChildMap.containsKey(varName)) {
                    String parentType = parentChildMap.get(varName);
                    CastExpr castExpr = new CastExpr(new ClassOrInterfaceType(null, parentType), n.clone());
                    n.replace(castExpr);
                }
            }
        }, null);
    }
    public static void PCD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(CastExpr n, Void arg) {
                super.visit(n, arg);
                Expression innerExpr = n.getExpression();
                n.replace(innerExpr); // Remove the cast expression and replace it with the inner expression
            }
        }, null);
    }
    // PCD2: Type Cast Operator Deletion(with conditions of PCI)
//    public static void applyPCD(CompilationUnit cu) {
//        Map<String, String> parentChildMap = new HashMap<>();
//
//        // Step 1: Collect variables declared with parent types but initialized with child types
//        cu.accept(new VoidVisitorAdapter<Void>() {
//            @Override
//            public void visit(VariableDeclarator n, Void arg) {
//                super.visit(n, arg);
//                if (n.getInitializer().isPresent()) {
//                    Expression initializer = n.getInitializer().get();
//                    if (initializer instanceof ObjectCreationExpr) {
//                        ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) initializer;
//                        String parentType = n.getTypeAsString();
//                        String childType = objectCreationExpr.getTypeAsString();
//                        if (!parentType.equals(childType)) {
//                            parentChildMap.put(n.getNameAsString(), parentType);
//                        }
//                    }
//                }
//            }
//        }, null);
//
//        // Step 2: Remove type casting for relevant variables
//        cu.accept(new VoidVisitorAdapter<Void>() {
//            @Override
//            public void visit(CastExpr n, Void arg) {
//                super.visit(n, arg);
//                Expression innerExpr = n.getExpression();
//
//                // Check if the inner expression matches a variable in the parentChildMap
//                if (innerExpr instanceof NameExpr) {
//                    NameExpr nameExpr = (NameExpr) innerExpr;
//                    String varName = nameExpr.getNameAsString();
//                    if (parentChildMap.containsKey(varName)) {
//                        n.replace(innerExpr); // Remove the cast and replace it with the inner expression
//                    }
//                }
//            }
//        }, null);
//    }
    public static void PCC(CompilationUnit cu) {
        Map<String, String> selfConstructedMap = new HashMap<>();
        Map<String, List<String>> parentToChildrenMap = new HashMap<>();
        // Step 1: Collect variables declared with their own type and initialized using their own constructor
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator n, Void arg) {
                super.visit(n, arg);
                if (n.getInitializer().isPresent()) {
                    Expression initializer = n.getInitializer().get();
                    if (initializer instanceof ObjectCreationExpr) {
                        ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) initializer;
                        String declaredType = n.getTypeAsString();
                        String constructedType = objectCreationExpr.getTypeAsString();
                        if (declaredType.equals(constructedType)) {
                            selfConstructedMap.put(n.getNameAsString(), declaredType);
                        }
                    }
                }
            }
        }, null);
        // Step 2: Build the map of parent-to-children relationships dynamically
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                if (n.getExtendedTypes().isNonEmpty()) {
                    String childType = n.getNameAsString();
                    n.getExtendedTypes().forEach(parent -> {
                        String parentType = parent.getNameAsString();
                        parentToChildrenMap.computeIfAbsent(parentType, k -> new ArrayList<>()).add(childType);
                    });
                }
            }
        }, null);
        // Step 3: Replace the first type cast to a child with another valid child type
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(CastExpr n, Void arg) {
                super.visit(n, arg);

                Expression innerExpr = n.getExpression();
                if (innerExpr instanceof NameExpr) {
                    NameExpr nameExpr = (NameExpr) innerExpr;
                    String varName = nameExpr.getNameAsString();

                    // Check if the variable matches selfConstructedMap
                    if (selfConstructedMap.containsKey(varName)) {
                        String parentType = selfConstructedMap.get(varName);

                        // Get child classes of the parent type
                        List<String> childTypes = parentToChildrenMap.getOrDefault(parentType, List.of());
                        if (!childTypes.isEmpty()) {
                            String currentCastType = n.getTypeAsString();

                            // Find an alternative child type to replace the current one
                            for (String childType : childTypes) {
                                if (!childType.equals(currentCastType)) {
                                    n.setType(childType); // Change the cast type
                                    System.out.println("Changed cast type: " + currentCastType + " -> " + childType);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }, null);
    }
    public static void PRV(CompilationUnit cu) {
        // Step 1: Collect information about declared variables and their types
        Map<String, String> variableToTypeMap = new HashMap<>(); // Tracks variable names and their types
        Map<String, List<String>> parentToChildrenMap = new HashMap<>(); // Tracks parent to child relationships

        // Collect variable declarations
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator n, Void arg) {
                super.visit(n, arg);
                String varName = n.getNameAsString();
                String varType = n.getTypeAsString();

                variableToTypeMap.put(varName, varType);

                // Check if the initializer is a child type
                if (n.getInitializer().isPresent() && n.getInitializer().get() instanceof ObjectCreationExpr) {
                    ObjectCreationExpr initializer = (ObjectCreationExpr) n.getInitializer().get();
                    String initializerType = initializer.getTypeAsString();
                    parentToChildrenMap.computeIfAbsent(varType, k -> new ArrayList<>()).add(initializerType);
                }
            }
        }, null);
        // Step 2: Modify assignment statements
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(AssignExpr n, Void arg) {
                super.visit(n, arg);

                // Check if the left-hand side is a variable of a parent type
                if (n.getTarget() instanceof NameExpr) {
                    NameExpr target = (NameExpr) n.getTarget();
                    String targetVar = target.getNameAsString();

                    if (variableToTypeMap.containsKey(targetVar)) {
                        String targetType = variableToTypeMap.get(targetVar);

                        // Check if the right-hand side is a child object
                        Expression value = n.getValue();
                        if (value instanceof NameExpr) {
                            NameExpr valueExpr = (NameExpr) value;
                            String valueVar = valueExpr.getNameAsString();

                            if (variableToTypeMap.containsKey(valueVar)) {
                                String valueType = variableToTypeMap.get(valueVar);

                                // Find an alternative compatible child type
                                List<String> children = parentToChildrenMap.getOrDefault(targetType, new ArrayList<>());
                                for (String childType : children) {
                                    if (!childType.equals(valueType)) {
                                        // Find a variable of the alternative child type
                                        for (Map.Entry<String, String> entry : variableToTypeMap.entrySet()) {
                                            if (entry.getValue().equals(childType)) {
                                                n.setValue(new NameExpr(entry.getKey()));
                                                System.out.println("Changed assignment: " + valueVar + " -> " + entry.getKey());
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, null);
    }
    public static void OMR(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                super.visit(clazz, arg);

                // نقشه‌ای برای ذخیره متدهای اورلود‌شده
                Map<String, List<MethodDeclaration>> overloadedMethods = new HashMap<>();

                // شناسایی متدهای اورلود‌شده داخل همان کلاس
                clazz.getMethods().forEach(method -> {
                    String methodName = method.getNameAsString();
                    overloadedMethods
                            .computeIfAbsent(methodName, k -> new ArrayList<>())
                            .add(method);
                });

                // تعویض بدنه‌های متدهای اورلود‌شده
                for (List<MethodDeclaration> methods : overloadedMethods.values()) {
                    if (methods.size() > 1) {
                        // تعویض بدنه‌ها فقط بین متدهای اورلود‌شده در همان کلاس
                        BlockStmt firstBody = methods.get(0).getBody().orElse(null);
                        for (int i = 1; i < methods.size(); i++) {
                            BlockStmt currentBody = methods.get(i).getBody().orElse(null);

                            if (firstBody != null && currentBody != null) {
                                // تعویض بدنه‌های متد
                                methods.get(i).setBody(firstBody.clone());
                                firstBody = currentBody;
                            }
                        }
                        // آخرین بدنه جایگزین بدنه اولین متد می‌شود
                        methods.get(0).setBody(firstBody.clone());
                    }
                }
            }
        }, null);
    }
    public static void OMD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                super.visit(clazz, arg);

                // نقشه‌ای برای ذخیره متدهای اورلود‌شده
                Map<String, List<MethodDeclaration>> overloadedMethods = new HashMap<>();

                // شناسایی متدهای اورلود‌شده داخل همان کلاس
                clazz.getMethods().forEach(method -> {
                    String methodName = method.getNameAsString();
                    overloadedMethods
                            .computeIfAbsent(methodName, k -> new ArrayList<>())
                            .add(method);
                });

                // حذف یا کامنت‌گذاری متدهای اورلود‌شده
                for (List<MethodDeclaration> methods : overloadedMethods.values()) {
                    if (methods.size() > 1) {
                        // نگه داشتن اولین متد و حذف یا کامنت کردن بقیه
                        for (int i = 1; i < methods.size(); i++) {
                            MethodDeclaration methodToDelete = methods.get(i);

                            // روش اول: حذف متد
                            methodToDelete.remove();

                            // روش دوم: کامنت کردن متد
                            // methodToDelete.setComment(new JavadocComment("This method was removed by OMD mutation operator."));
                        }
                    }
                }
            }
        }, null);
    }
    public static void OAC(CompilationUnit cu) {
        // یافتن متدهای اورلود شده
        Map<String, List<MethodDeclaration>> overloadedMethods = new HashMap<>();

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                String methodName = n.getNameAsString();
                overloadedMethods
                        .computeIfAbsent(methodName, k -> new ArrayList<>())
                        .add(n);
            }
        }, null);

        // تغییر ترتیب آرگومان‌های فراخوانی‌ها
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);
                String methodName = n.getNameAsString();

                // بررسی اینکه متد اورلود شده‌ای برای این فراخوانی وجود دارد
                List<MethodDeclaration> methods = overloadedMethods.get(methodName);
                if (methods != null && methods.size() > 1) {
                    // پیدا کردن متدی با تعداد آرگومان‌های متفاوت
                    for (MethodDeclaration method : methods) {
                        if (method.getParameters().size() == n.getArguments().size() + 1) {
                            // افزودن یک آرگومان پیش‌فرض به فراخوانی
                            n.addArgument("null"); // یا آرگومان پیش‌فرض دیگری
                            break;
                        }
                    }
                }
            }
        }, null);
    }
    public static void JTI(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(NameExpr n, Void arg) {
                super.visit(n, arg);

                // بررسی اینکه آیا این نام مربوط به یک عضو کلاس است
                if (isClassMember(n)) {
                    // اضافه کردن this. به قبل از نام
                    FieldAccessExpr fieldAccess = new FieldAccessExpr(new ThisExpr(), n.getNameAsString());
                    n.replace(fieldAccess);
                }
            }

            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);

                // بررسی اینکه آیا فراخوانی متد مربوط به یک متد عضو کلاس است
                if (!n.getScope().isPresent() && isClassMethod(n)) {
                    // اضافه کردن this. به قبل از فراخوانی متد
                    n.setScope(new ThisExpr());
                }
            }

            // متد کمکی برای بررسی اینکه آیا نام مربوط به یک عضو کلاس است
            private boolean isClassMember(NameExpr n) {
                // گرفتن کلاس والد
                Optional<ClassOrInterfaceDeclaration> parentClass = n.findAncestor(ClassOrInterfaceDeclaration.class);
                if (parentClass.isPresent()) {
                    ClassOrInterfaceDeclaration clazz = parentClass.get();
                    // بررسی اینکه آیا این نام با یکی از فیلدهای کلاس تطابق دارد
                    return clazz.getFields().stream()
                            .flatMap(field -> field.getVariables().stream())
                            .anyMatch(var -> var.getNameAsString().equals(n.getNameAsString()));
                }
                return false;
            }

            // متد کمکی برای بررسی اینکه آیا فراخوانی متد مربوط به یک متد عضو کلاس است
            private boolean isClassMethod(MethodCallExpr n) {
                // گرفتن کلاس والد
                Optional<ClassOrInterfaceDeclaration> parentClass = n.findAncestor(ClassOrInterfaceDeclaration.class);
                if (parentClass.isPresent()) {
                    ClassOrInterfaceDeclaration clazz = parentClass.get();
                    // بررسی اینکه آیا این متد در کلاس تعریف شده است
                    return clazz.getMethodsByName(n.getNameAsString()).size() > 0;
                }
                return false;
            }
        }, null);
    }
    public static void JTD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldAccessExpr n, Void arg) {
                super.visit(n, arg);

                // بررسی اینکه آیا کلمه کلیدی this قابل حذف است
                if (n.getScope().isThisExpr()) {
                    NameExpr newName = new NameExpr(n.getNameAsString());
                    n.replace(newName); // جایگزینی با نام ساده بدون this
                }
            }

            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);

                // بررسی اینکه آیا فراخوانی متد شامل this است
                if (n.getScope().isPresent() && n.getScope().get().isThisExpr()) {
                    n.removeScope(); // حذف this از قبل فراخوانی متد
                }
            }
        }, null);
    }
    public static void JSI(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg);

                // اگر متغیر static نیست، آن را static کنیم
                if (!n.isStatic()) {
                    n.addModifier(Modifier.Keyword.STATIC);
                }
            }
        }, null);
    }
    public static void JSD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                // حذف تمام مودفایرها
                n.getModifiers().clear();
            }

            @Override
            public void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg);
                // حذف تمام مودفایرها
                n.getModifiers().clear();
            }

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                // حذف تمام مودفایرها
                n.getModifiers().clear();
            }
        }, null);
    }
    public static void JID(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg);
                // حذف مقداردهی اولیه از متغیرها
                n.getVariables().forEach(variable -> {
                    if (variable.getInitializer().isPresent()) {
                        Expression emptyExpr = null;  // برای حذف مقداردهی اولیه
                        variable.setInitializer(emptyExpr);  // حذف مقداردهی اولیه
                    }
                });
            }
        }, null);
    }
    public static void JDC(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);

                // جمع‌آوری سازنده‌های بدون پارامتر در لیستی موقت
                List<ConstructorDeclaration> constructorsToRemove = n.getConstructors().stream()
                        .filter(constructor -> constructor.getParameters().isEmpty()) // فقط بدون پارامترها
                        .toList();

                // حذف سازنده‌های بدون پارامتر پس از اتمام پیمایش
                constructorsToRemove.forEach(ConstructorDeclaration::remove);
            }
        }, null);
    }
    public static void IHI(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                if (!n.isInterface() && !n.getExtendedTypes().isEmpty()) {
                    // Find parent class name
                    String parentClassName = n.getExtendedTypes(0).getNameAsString();

                    // Find the parent class in the CompilationUnit
                    cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                            .filter(parent -> parent.getNameAsString().equals(parentClassName))
                            .findFirst()
                            .ifPresent(parentClass -> {
                                // Get field names of the child class
                                Set<String> childFieldNames = n.findAll(FieldDeclaration.class).stream()
                                        .flatMap(field -> field.getVariables().stream())
                                        .map(VariableDeclarator::getNameAsString)
                                        .collect(Collectors.toSet());

                                // Copy fields from parent class to the child class only if not already present
                                parentClass.findAll(FieldDeclaration.class).forEach(field -> {
                                    field.getVariables().forEach(variable -> {
                                        if (!childFieldNames.contains(variable.getNameAsString())) {
                                            n.getMembers().add(0, field.clone()); // Add field at the beginning of the class
                                        }
                                    });
                                });
                            });
                }
            }
        }, null);
    }
    public static void IHD(CompilationUnit cu) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                if (!n.isInterface() && !n.getExtendedTypes().isEmpty()) {
                    // Find parent class name
                    String parentClassName = n.getExtendedTypes(0).getNameAsString();

                    // Find the parent class in the CompilationUnit
                    cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                            .filter(parent -> parent.getNameAsString().equals(parentClassName))
                            .findFirst()
                            .ifPresent(parentClass -> {
                                // Remove child fields that exist in the parent class
                                List<String> parentFieldNames = parentClass.findAll(VariableDeclarator.class).stream()
                                        .map(VariableDeclarator::getNameAsString)
                                        .toList();

                                n.findAll(FieldDeclaration.class).forEach(childField -> {
                                    // Check if any of the variables match parent field names
                                    childField.getVariables().removeIf(variable ->
                                            parentFieldNames.contains(variable.getNameAsString()));

                                    // If the child field has no remaining variables, remove the entire field
                                    if (childField.getVariables().isEmpty()) {
                                        childField.remove();
                                    }
                                });
                            });
                }
            }
        }, null);
    }
}
