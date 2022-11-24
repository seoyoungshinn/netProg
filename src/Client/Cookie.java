package Client;
import javax.swing.JPanel;

public class Cookie extends JPanel {
	private static final long serialVersionUID = 1L;
int size;
   String map[][];

   public Cookie(int size) {
      this.size = size;
      map = new String[size][size];
      this.setLayout(null);
      this.setBounds(0, 0, 600, 600);

   }

}