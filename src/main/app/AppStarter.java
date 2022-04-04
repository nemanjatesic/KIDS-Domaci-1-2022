package main.app;

import javafx.application.Application;
import main.input.FileInput;
import main.input.FileReader;
import main.logger.Logger;

import java.io.File;
import java.util.List;

public class AppStarter {

	public static void main(String[] args) throws Exception {
		Logger.debugEnabled = List.of(args).stream().anyMatch(s -> s.equals("debugEnabled"));

		System.out.println(Math.ceil(5));;
//		FileReader fileReader = new FileReader(new File("G:\\KIDS\\2022\\PrviDomaci\\data\\disk1\\A\\wiki-1.txt"));
//		var a = fileReader.call();
//		System.out.println(String.format("[%s]", a.getContent().substring(0, 10)));
//		System.out.println(a.getContent().length());
//		System.out.println(a.getContent().replace("\r", "").replace("\n", " ").length());
//		System.out.println(a.getContent().replace("\r\n", " ").length());
//		System.out.println(a.getContent().replace("\n", " ").length());

		Application.launch(App.class);
	}

}
