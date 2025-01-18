import cohere
import random
import time
import ssl

# غیرفعال کردن بررسی‌های SSL
ssl._create_default_https_context = ssl._create_unverified_context

# Set API key for Cohere
co = cohere.Client('sky')  # Your API key

def read_java_file(file_path):
    """Read Java file contents"""
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except FileNotFoundError:
        print("File not found!")
        return None

def suggest_mutation_operators(java_code):
    """Suggest mutation operators for the Java code"""
    prompt = f"""
    You are an expert in Object Oriented Mutation Testing (Encapsulation, Inheritance, Polymorphism, Java-Specific). Analyze the following Java code and suggest appropriate mutation operators
    that could be applied to test the code effectively and they are the best choices. Suggest 4-6 operators from the following list only: AMC(Access Modifier Change), IHI(Hiding Variable Insertion), IHD(Hiding Variable Deletion), IOD(Overriding Method Deletion), IOP(Overridden Method Calling Position Change), IOR(Overridden Method Rename), ISI(Super Keyword Insertion), ISD(Super Keyword Deletion), IPC(Explicit Parent Constructor Deletion), PNC(new Method Call With Child Class Type), PMD(Member Variable Declaration with Parent Class Type), PPD(Parameter Variable Declaration with Child Class Type), PCI(Type Cast Operator Insertion), PCD(Type Cast Operator Deletion), PCC(Cast Type Change), PRV(Reference Assignment with Other Compatible Type), OMR(Overloading Method Contents Replace), OMD(Overloading Method Deletion), OAC(Arguments of Overloading Method Call Change), JTI(this Keyword Insertion), JTD(this Keyword Deletion), JSI(Static Modifier Insertion), JSD(Static Modifier Deletion), JID(Member Variable Initialization Deletion), JDC(Java-supported Default Constructor Deletion). Explain why those operators are suitable:

java
    {java_code}
    """
    
    attempt = 0
    while attempt < 3:  # Retry up to 3 times
        try:
            # Call Cohere API for suggestions
            response = co.generate(
                model='command-xlarge-nightly',
                prompt=prompt,
                max_tokens=500,
                temperature=0.5
            )
            return response.generations[0].text.strip()
        except Exception as e:
            print(f"Attempt {attempt + 1}: Error occurred while generating suggestions: {e}")
            time.sleep(2)  # Wait for 2 seconds before retrying
            attempt += 1
    
    # If failed after 3 attempts, return a message
    return "An error occurred while generating suggestions after multiple attempts."

def extract_operators(suggestions):
    """Extract mutation operators from the model's suggestions"""
    valid_operators = [
        "AMC", "IHI", "IHD", "IOD", "IOP", "IOR", "ISI", "ISD", "IPC",
        "PNC", "PMD", "PPD", "PCI", "PCD", "PCC", "PRV", "OMR", "OMD",
        "OAC", "JTI", "JTD", "JSI", "JSD", "JID", "JDC"
    ]
    operators = [op for op in valid_operators if op in suggestions]
    return operators

def select_random_operators(operators, count=4):
    """Select random mutation operators from the list"""
    return random.sample(operators, min(count, len(operators)))

def save_to_file(output_path, operators):
    """Save selected operators to a file"""
    try:
        with open(output_path, "w", encoding="utf-8") as file:
            file.write(", ".join(operators))
        print(f"Suggestions successfully saved to: {output_path}")
    except Exception as e:
        print(f"Error saving file: {e}")

def main():
    print("Please enter the Java file path:")
    file_path = "./code.java"  # Update path as needed
    output_path = "mutation_suggestions.txt"  # Output file for suggestions

    java_code = read_java_file(file_path)
    if java_code:
        print("\nAnalyzing code...\n")
        suggestions = suggest_mutation_operators(java_code)
        if suggestions:
            print("Model's full response:")
            print(suggestions)  # Print full model response
            operators = extract_operators(suggestions)
            if operators:
                selected_operators = select_random_operators(operators)
                save_to_file(output_path, selected_operators)
                print("Selected operators:", ", ".join(selected_operators))
            else:
                print("No suitable operators were suggested.")
        else:
            print("No response from the model.")

if __name__ == "__main__":
    main()
