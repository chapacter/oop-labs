import myfirstpackage.*;

class MyFirstClass {
    public static void main(String[] s) {
        MyFirstPackage o = new MyFirstPackage(22, 3);

        // Вывод остатка от деления
        System.out.println("Остаток от деления: " + o.func());

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                o.setA(i);
                o.setB(j);
                System.out.print(o.func() + " ");
            }
            System.out.println();
        }
    }
}
