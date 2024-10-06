package com.javapractice.other;

/**
 * @ClassName: initargsTest
 * @Description:
 * @Author: Kanra
 * @Date: 2024/08/13
 */
public class InitargsTest {

    // 可変長引数を使用して、任意の数の引数を受け取る
    public int sum(int... numbers) {
        int total = 0;
        for (int number : numbers) {
            total += number; // 引数を一つずつ合計
        }
        return total;
    }

    public static void main(String[] args) {
        InitargsTest calculator = new InitargsTest();

        // 引数を一つだけ渡す
        int result1 = calculator.sum(10);
        System.out.println("Sum with one argument: " + result1); // 10

        // 引数を複数渡す
        int result2 = calculator.sum(10, 20, 30);
        System.out.println("Sum with multiple arguments: " + result2); // 60

        // 引数を渡さない場合
        int result3 = calculator.sum();
        System.out.println("Sum with no arguments: " + result3); // 0
    }
}
