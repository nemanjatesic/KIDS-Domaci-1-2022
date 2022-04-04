package main.app;

import javafx.application.Application;
import main.cruncher.ListOfWords;
import main.input.FileInput;
import main.input.FileReader;
import main.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppStarter {

	public static void main(String[] args) throws Exception {
		Logger.debugEnabled = List.of(args).stream().anyMatch(s -> s.equals("debugEnabled"));

		System.out.println(Math.ceil(5));
		;
		// FileReader fileReader = new FileReader(new
		// File("G:\\KIDS\\2022\\PrviDomaci\\data\\disk1\\A\\wiki-1.txt"));
		// var a = fileReader.call();
		// System.out.println(String.format("[%s]", a.getContent().substring(0, 10)));
		// System.out.println(a.getContent().length());
		// System.out.println(a.getContent().replace("\r", "").replace("\n", "
		// ").length());
		// System.out.println(a.getContent().replace("\r\n", " ").length());
		// System.out.println(a.getContent().replace("\n", " ").length());

		// test();
		test1();
		System.out.println();
		test2();

		Application.launch(App.class);
	}

	public static void test() {
		String s = "cao nesto poz nesto kako si ti mala si moja nesto cao nesto nesto nesto";
		// 0123456789

		Map<ListOfWords<Integer>, Integer> toReturn = new HashMap<>();
		int start = 0;
		int end = s.length();
		int arity = 1;

		ListOfWords<Integer> listOfWords = new ListOfWords<>();
		List<String> tmp = new ArrayList<>();

		StringBuilder stringBuilder = new StringBuilder("");
		int currentWord = 0;

		for (int i = start; i < end; i++) {
			char ch = s.charAt(i);

			if (ch == ' ' || ch == '\n' || i == end) {
				currentWord++;
				String str = stringBuilder.toString();
				stringBuilder.delete(0, str.length());
				listOfWords.addToList(str.hashCode());
				tmp.add(str);
				if (currentWord == arity) {
					if (listOfWords.getList().size() == arity) {
						toReturn.putIfAbsent(listOfWords, 0);
						toReturn.put(listOfWords, toReturn.get(listOfWords) + 1);
					}

					currentWord = 0;

					if (i + 1 == end) {
						break;
					}

					int toRemove = 0;
					for (int k = 1; k < tmp.size(); k++) {
						toRemove = toRemove + (tmp.get(k).length() + 1);
					}

					i = i - toRemove;
					listOfWords = new ListOfWords<>();
					tmp = new ArrayList<>();
				}

				if (i == end) {
					break;
				}
			} else {
				stringBuilder.append(ch);
			}
		}

		toReturn.keySet().forEach(key -> {
			key.getList().forEach(str -> System.out.print(str + " "));
			System.out.print(", -> ");
			System.out.println(toReturn.get(key));
		});
	}

	public static void test1() {
		String s = "cao nesto poz nesto kako si ti mala si moja nesto cao nesto nesto nesto";

		Map<ListOfWords<Integer>, Integer> toReturn = new HashMap<>();
		int start = 0;
		int end = s.length();
		int arity = 2;

		ListOfWords<Integer> listOfWords = new ListOfWords<>();
		List<String> tmp = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder("");
		int currentWord = 0;
		char ch;
		int toRemove = 0;

		for (int i = start; i < end; i++) {
			ch = s.charAt(i);

			if (ch == ' ') {
				String str = stringBuilder.toString();
				stringBuilder.delete(0, str.length());
				listOfWords.addToList(str.hashCode());
				tmp.add(str);
				currentWord++;

				if (currentWord == arity) {
					toReturn.putIfAbsent(listOfWords, 0);
					toReturn.put(listOfWords, toReturn.get(listOfWords) + 1);

					listOfWords = new ListOfWords<>();
					currentWord = 0;

					toRemove = 0;
					for (int k = 1; k < tmp.size(); k++) {
						toRemove = toRemove + (tmp.get(k).length() + 1);
					}
					// if (toRemove > 0) {
					// toRemove++;
					// }

					i = i - toRemove;
					tmp.clear();
				}
			} else {
				stringBuilder.append(ch);
			}
		}

		toReturn.keySet().forEach(key -> {
			key.getList().forEach(str -> System.out.print(str + " "));
			System.out.print(", -> ");
			System.out.println(toReturn.get(key));
		});
	}

	public static void test2() {
		String s = "cao nesto poz nesto kako si ti mala si moja nesto cao nesto nesto nesto";

		Map<ListOfWords<Integer>, Integer> toReturn = new HashMap<>();
		int start = 0;
		int end = s.length();
		int arity = 2;

		ListOfWords<Integer> listOfWords = new ListOfWords<>();
		StringBuilder stringBuilder = new StringBuilder("");
		int currentWord = 0;
		char ch;
		int toRemove = 0;

		for (int i = start; i < end; i++) {
			ch = s.charAt(i);
			if (currentWord != 0) {
				toRemove++;
			}

			if (ch == ' ') {
				String str = stringBuilder.toString();
				stringBuilder.delete(0, str.length());
				listOfWords.addToList(str.hashCode());
				currentWord++;

				if (currentWord == arity) {
					toReturn.putIfAbsent(listOfWords, 0);
					toReturn.put(listOfWords, toReturn.get(listOfWords) + 1);

					listOfWords = new ListOfWords<>();
					currentWord = 0;

					i = i - toRemove;
					toRemove = 0;
				}
			} else {
				stringBuilder.append(ch);
			}
		}

		toReturn.keySet().forEach(key -> {
			key.getList().forEach(str -> System.out.print(str + " "));
			System.out.print(", -> ");
			System.out.println(toReturn.get(key));
		});
	}

}
