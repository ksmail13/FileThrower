package dropbox.ui;

import javax.swing.*;
import java.awt.*;

public class MyScrollPanel extends JScrollPane {
	JScrollPane scrollPanel;

	public MyScrollPanel(Container arg0) {
		scrollPanel = new JScrollPane(arg0,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
