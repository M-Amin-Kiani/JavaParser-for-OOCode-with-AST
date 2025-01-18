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
        System.out.println("Do you want to proceed with your own mutation types or our suggestion? (type 'own' or 'suggestion'): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        List<String> mutationTypes = new ArrayList<>();

        if ("own".equals(choice)) {
            System.out.println("Enter mutation types (comma-separated, e.g., AMC,IHI,IHD): ");
            String input = scanner.nextLine();

            if (!input.trim().isEmpty()) {
                for (String type : input.split(",")) {
                    mutationTypes.add(type.trim().toUpperCase());
                }
            } else {
                System.out.println("No mutation types provided. Exiting.");
                return;
            }

        } else if ("suggestion".equals(choice)) {
            String suggestionFilePath = "./suggestions.txt";
            try {
                List<String> lines = Files.readAllLines(Paths.get(suggestionFilePath));
                if (!lines.isEmpty()) {
                    String firstLine = lines.get(0);
                    for (String type : firstLine.split(",")) {
                        mutationTypes.add(type.trim().toUpperCase());
                    }
                } else {
                    System.out.println("Suggestion file is empty. Exiting.");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Error reading suggestions file: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Invalid choice. Exiting.");
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
                MutationOperators.AMC(cu);
                System.out.println(green + "AMC applied" + reset);
            };
            case "IHI" -> mutationAction = () -> {
                MutationOperators.IHI(cu);
                System.out.println(green + "IHI applied" + reset);
            };
            case "IHD" -> mutationAction = () -> {
                MutationOperators.IHD(cu);
                System.out.println(green + "IHD applied" + reset);
            };
            case "IOD" -> mutationAction = () -> {
                MutationOperators.IOD(cu);
                System.out.println(green + "IOD applied" + reset);
            };
            case "IOP" -> mutationAction = () -> {
                MutationOperators.IOP(cu);
                System.out.println(green + "IOP applied" + reset);
            };
            case "IOR" -> mutationAction = () -> {
                MutationOperators.IOR(cu);
                System.out.println(green + "IOR applied" + reset);
            };
            case "ISI" -> mutationAction = () -> {
                MutationOperators.ISI(cu);
                System.out.println(green + "ISI applied" + reset);
            };
            case "ISD" -> mutationAction = () -> {
                MutationOperators.ISD(cu);
                System.out.println(green + "ISD applied" + reset);
            };
            case "IPC" -> mutationAction = () -> {
                MutationOperators.IPC(cu);
                System.out.println(green + "IPC applied" + reset);
            };
            case "PNC" -> mutationAction = () -> {
                MutationOperators.PNC(cu);
                System.out.println(green + "PNC applied" + reset);
            };
            case "PMD" -> mutationAction = () -> {
                MutationOperators.PMD(cu);
                System.out.println(green + "PMD applied" + reset);
            };
            case "PPD" -> mutationAction = () -> {
                MutationOperators.PPD(cu);
                System.out.println(green + "PPD applied" + reset);
            };
            case "PCI" -> mutationAction = () -> {
                MutationOperators.PCI(cu);
                System.out.println(green + "PCI applied" + reset);
            };
            case "PCD" -> mutationAction = () -> {
                MutationOperators.PCD(cu);
                System.out.println(green + "PCD applied" + reset);
            };
            case "PCC" -> mutationAction = () -> {
                MutationOperators.PCC(cu);
                System.out.println(green + "PCC applied" + reset);
            };
            case "PRV" -> mutationAction = () -> {
                MutationOperators.PRV(cu);
                System.out.println(green + "PRV applied" + reset);
            };
            case "OMR" -> mutationAction = () -> {
                MutationOperators.OMR(cu);
                System.out.println(green + "OMR applied" + reset);
            };
            case "OMD" -> mutationAction = () -> {
                MutationOperators.OMD(cu);
                System.out.println(green + "OMD applied" + reset);
            };
            case "OAC" -> mutationAction = () -> {
                MutationOperators.OAC(cu);
                System.out.println(green + "OAC applied" + reset);
            };
            case "JTI" -> mutationAction = () -> {
                MutationOperators.JTI(cu);
                System.out.println(green + "JTI applied" + reset);
            };
            case "JTD" -> mutationAction = () -> {
                MutationOperators.JTD(cu);
                System.out.println(green + "JTD applied" + reset);
            };
            case "JSI" -> mutationAction = () -> {
                MutationOperators.JSI(cu);
                System.out.println(green + "JSI applied" + reset);
            };
            case "JSD" -> mutationAction = () -> {
                MutationOperators.JSD(cu);
                System.out.println(green + "JSD applied" + reset);
            };
            case "JID" -> mutationAction = () -> {
                MutationOperators.JID(cu);
                System.out.println(green + "JID applied" + reset);
            };
            case "JDC" -> mutationAction = () -> {
                MutationOperators.JDC(cu);
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

