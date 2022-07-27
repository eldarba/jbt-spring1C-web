package demos;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Demo1 {

	public static void main(String[] args) {
		// Path is an interface. it is like File
		Path path;
		path = Paths.get("c:/temp").resolve("apple.jpg");
		System.out.println(path.toAbsolutePath());
		// same:
		path = Paths.get("/temp");
		System.out.println(path.toAbsolutePath());

		// current directory - where the application executing (the project directory)
		path = Paths.get(".");
		System.out.println(path.toAbsolutePath());

		// Parent directory - relative to where the application executing (the project
		// directory)
		path = Paths.get("../another-project");
		System.out.println(path.toAbsolutePath());

//		System.out.println(StringUtils.cleanPath("c:/./temp/.."));

	}

}
