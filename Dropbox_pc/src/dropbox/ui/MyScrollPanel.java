package dropbox.ui;

import java.awt.Container;
import javax.swing.JScrollPane;

public class MyScrollPanel extends JScrollPane {
	JScrollPane scrollPanel;

	public MyScrollPanel(Container arg0) {
		scrollPanel = new JScrollPane(arg0,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
