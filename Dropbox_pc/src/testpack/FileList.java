package testpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class FileList {
	// Fileds
	String fileName; // Ű����� �Էµ� ���� ����
	String filePath = "/Users/heejoongkim/monitor";
	private String[] fileNameArr;

	// Ű����� Ư�� ���� �Է�
	public void fileInput() {
		try {
			InputStreamReader reader = new InputStreamReader(System.in);
			BufferedReader inputBuffer = new BufferedReader(reader);

			System.out.print("File Name = ");
			fileName = inputBuffer.readLine();
		} catch (Exception ex) {
		}
	}

	// ���� ���� ���
	public void fileInfo() {
		File f = new File(filePath, fileName);
		double fileSize;
		Date date;
		System.out.println("-------------------------");
		// ���� ���� �Ǻ�
		if (!f.exists()) {
			System.out.print(f.getParent());
			System.out.println(" ���� ������ �������� �ʽ��ϴ�.");
			return;
		}

		// �б� �����ΰ�
		if (f.canWrite()) {
			System.out.println("���� �ֽ��ϴ�.");
		} else {
			System.out.println("�б� ���� �Դϴ�.");
		}

		// �ش� ������ ���� ��� ǥ��
		System.out.println("Path = " + f.getAbsolutePath());

		// ���� ������
		date = new Date(f.lastModified());
		System.out.println("Last Modified = " + date.toString());

		// ������ ũ�� (Mb)
		fileSize = f.length() / 1000;
		System.out.println("File Size = " + fileSize + "Kb");
		System.out.println("-------------------------");
	}

	// Ư�� ���丮�� ��� ���� ���
	public void allListFile() {
		File path = new File(filePath);

		String files[] = path.list();
		fileNameArr = path.list();
//		for (int i = 0; i < files.length; i++) {
//			System.out.println(files[i]);
//		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//MyFrame myFrame = new MyFrame();

	}

	public String[] getFileNameArr() {
		// TODO Auto-generated method stub
		return fileNameArr;
	}



}
