public class Pitch {
    public native int lol_func(int x, int y);

    public static void main(String[] args) {
        System.load("/home/ejiek/Documents/Bachelor/jni_cli/libpitch.so");
	Pitch pitch = new Pitch();
        System.out.println("sum: "+ pitch.lol_func(2,2));
    }
}
