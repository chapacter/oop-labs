class MyFirstClass {
    public static void main(String[] s) {
        MySecondClass o = new MySecondClass(22, 3);

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

class MySecondClass {
    private int a;
    private int b;

    public MySecondClass(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public void setA(int a) { this.a = a; }
    public void setB(int b) { this.b = b; }

    public int getA() { return a; }
    public int getB() { return b; }

    public int func() {
        if (b == 0) { // Защита от деления на ноль
            return 0;
        }
        return a % b;
    }
}
