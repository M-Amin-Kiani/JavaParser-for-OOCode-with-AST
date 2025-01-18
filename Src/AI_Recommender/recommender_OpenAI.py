import openai
import sys
sys.stdout.reconfigure(encoding='utf-8')


# تنظیم کلید API
openai.api_key = "sky"


def read_java_file(file_path):
    """خواندن محتوای فایل جاوا"""
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except FileNotFoundError:
        print("no file founded!")
        return None


def suggest_mutation_operators(java_code):
    """پیشنهاد اپراتورهای میوتیشن برای کد جاوا"""
    prompt = f"""
    You are an expert in mutation testing. Analyze the following Java code and suggest appropriate mutation operators 
    that could be applied to test the code effectively. Explain why those operators are suitable:
    ```java
    {java_code}
    ```
    """
    try:
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",  # استفاده از مدل جدید
            messages=[
                {"role": "system", "content": "You are a helpful assistant specializing in mutation testing."},
                {"role": "user", "content": prompt},
            ]
        )
        return response['choices'][0]['message']['content'].strip()
    except Exception as e:
        return f"An error occurred while generating suggestions: {e}"


def main():
    while True:
        print("Please enter the Java file path or type 'exit' to quit:")
        file_path = input("File Path: ")
        if file_path.lower() in ["exit", "quit"]:
            print("exiting...")
            break
        
        java_code = read_java_file(file_path)
        if java_code:
            print("\n analyzing...\n")
            suggestions = suggest_mutation_operators(java_code)
            print("suggests:")
            print(suggestions)
            print("\n")


if __name__ == "__main__":
    main()
