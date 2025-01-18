package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String mutantsFolderPath = "src/main/java/mutants"; // پوشه ذخیره میوتنت‌ها

        // پاک کردن محتوای پوشه mutants در ابتدای برنامه
        try {
            File mutantsFolder = new File(mutantsFolderPath);
            if (mutantsFolder.exists()) {
                for (File file : mutantsFolder.listFiles()) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.toPath());
                    } else {
                        file.delete();
                    }
                }
            } else {
                mutantsFolder.mkdirs(); // ایجاد پوشه در صورت عدم وجود
            }
        } catch (Exception e) {
            System.err.println("Error while clearing mutants folder: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter mutation types (comma-separated, e.g., AMC,IHI,IHD): ");
        String input = scanner.nextLine();
        List<String> mutationTypes = new ArrayList<>();

        if (!input.trim().isEmpty()) {
            for (String type : input.split(",")) {
                mutationTypes.add(type.trim().toUpperCase());
            }
        } else {
            System.out.println("No mutation types provided. Exiting.");
            return;
        }

        try {

            String filePath = "src/main/java/org/example/code.java";

            File file = new File(filePath);
            JavaParser javaParser = new JavaParser();

            for (String mutationType : mutationTypes) {
                // Parse the file to create a fresh CompilationUnit for each mutation
                ParseResult<CompilationUnit> result = javaParser.parse(file);

                String testFilePath = "src/test/java/org/example/codeTest.java"; // مسیر فایل تست
                File testFile = new File(testFilePath);

                result.getResult().ifPresent(cu -> {
                    // Apply the mutation
                    applyMutation(cu, mutationType);

                    // Change the package name to the mutation type
                    cu.setPackageDeclaration("mutants." + mutationType);

                    // Generate the mutated code
                    String mutatedCode = cu.toString();

                    // Create a folder for the mutation type
                    String mutationFolderPath = mutantsFolderPath + "/" + mutationType;
                    File mutationFolder = new File(mutationFolderPath);
                    if (!mutationFolder.exists()) {
                        mutationFolder.mkdirs();
                    }

                    // Save the mutated file as code.java inside the mutation folder
                    String outputFilePath = mutationFolderPath + "/code.java";
                    try {
                        Files.write(Paths.get(outputFilePath), mutatedCode.getBytes());
                        System.out.println("Mutation " + mutationType + " applied and saved to: " + outputFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error saving the file for mutation: " + mutationType);
                    }

                    // Copy and update the test file for this mutation
                    try {
                        if (testFile.exists()) {
                            // Read the test file content
                            String testFileContent = new String(Files.readAllBytes(testFile.toPath()));

                            // Update the package name in the test file
                            testFileContent = testFileContent.replace(
                                    "package org.example;",
                                    "package mutants." + mutationType + ";"
                            );

                            // Save the updated test file in the mutation folder
                            String testOutputFilePath = mutationFolderPath + "/codeTest.java";
                            Files.write(Paths.get(testOutputFilePath), testFileContent.getBytes());
                            System.out.println("Test file saved to: " + testOutputFilePath);
                        } else {
                            System.out.println("Test file not found at: " + testFilePath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error saving the test file for mutation: " + mutationType);
                    }
                });

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static  void applyMutation(CompilationUnit cu,String mutationType) {
        String reset = "\u001B[0m"; // Reset color
        String green = "\u001B[32m"; // Green color
        String red = "\u001B[31m"; // Red color

        Runnable mutationAction;

        switch (mutationType) {
            case "AMC" -> mutationAction = () -> {
                MutationOperators.applyAMC(cu);
                System.out.println(green + "AMC applied" + reset);
            };
            case "IHI" -> mutationAction = () -> {
                MutationOperators.applyIHI(cu);
                System.out.println(green + "IHI applied" + reset);
            };
            case "IHD" -> mutationAction = () -> {
                MutationOperators.applyIHD(cu);
                System.out.println(green + "IHD applied" + reset);
            };
            case "IOD" -> mutationAction = () -> {
                MutationOperators.applyIOD(cu);
                System.out.println(green + "IOD applied" + reset);
            };
            case "IOP" -> mutationAction = () -> {
                MutationOperators.applyIOP(cu);
                System.out.println(green + "IOP applied" + reset);
            };
            case "IOR" -> mutationAction = () -> {
                MutationOperators.applyIOR(cu);
                System.out.println(green + "IOR applied" + reset);
            };
            case "ISI" -> mutationAction = () -> {
                MutationOperators.applyISI(cu);
                System.out.println(green + "ISI applied" + reset);
            };
            case "ISD" -> mutationAction = () -> {
                MutationOperators.applyISD(cu);
                System.out.println(green + "ISD applied" + reset);
            };
            case "IPC" -> mutationAction = () -> {
                MutationOperators.applyIPC(cu);
                System.out.println(green + "IPC applied" + reset);
            };
            case "PNC" -> mutationAction = () -> {
                MutationOperators.applyPNC(cu);
                System.out.println(green + "PNC applied" + reset);
            };
            case "PMD" -> mutationAction = () -> {
                MutationOperators.applyPMD(cu);
                System.out.println(green + "PMD applied" + reset);
            };
            case "PPD" -> mutationAction = () -> {
                MutationOperators.applyPPD(cu);
                System.out.println(green + "PPD applied" + reset);
            };
            case "PCI" -> mutationAction = () -> {
                MutationOperators.applyPCI(cu);
                System.out.println(green + "PCI applied" + reset);
            };
            case "PCD" -> mutationAction = () -> {
                MutationOperators.applyPCD(cu);
                System.out.println(green + "PCD applied" + reset);
            };
            case "PCC" -> mutationAction = () -> {
                MutationOperators.applyPCC(cu);
                System.out.println(green + "PCC applied" + reset);
            };
            case "PRV" -> mutationAction = () -> {
                MutationOperators.applyPRV(cu);
                System.out.println(green + "PRV applied" + reset);
            };
            case "OMR" -> mutationAction = () -> {
                MutationOperators.applyOMR(cu);
                System.out.println(green + "OMR applied" + reset);
            };
            case "OMD" -> mutationAction = () -> {
                MutationOperators.applyOMD(cu);
                System.out.println(green + "OMD applied" + reset);
            };
            case "OAC" -> mutationAction = () -> {
                MutationOperators.applyOAC(cu);
                System.out.println(green + "OAC applied" + reset);
            };
            case "JTI" -> mutationAction = () -> {
                MutationOperators.applyJTI(cu);
                System.out.println(green + "JTI applied" + reset);
            };
            case "JTD" -> mutationAction = () -> {
                MutationOperators.applyJTD(cu);
                System.out.println(green + "JTD applied" + reset);
            };
            case "JSI" -> mutationAction = () -> {
                MutationOperators.applyJSI(cu);
                System.out.println(green + "JSI applied" + reset);
            };
            case "JSD" -> mutationAction = () -> {
                MutationOperators.applyJSD(cu);
                System.out.println(green + "JSD applied" + reset);
            };
            case "JID" -> mutationAction = () -> {
                MutationOperators.applyJID(cu);
                System.out.println(green + "JID applied" + reset);
            };
            case "JDC" -> mutationAction = () -> {
                MutationOperators.applyJDC(cu);
                System.out.println(green + "JDC applied" + reset);
            };
            default -> mutationAction = () -> System.out.println(red + "Unknown mutation type: " + mutationType + reset);
        }

        mutationAction.run();
    }

    private static void deleteDirectory(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    deleteDirectory(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
        Files.delete(path);
    }
}

