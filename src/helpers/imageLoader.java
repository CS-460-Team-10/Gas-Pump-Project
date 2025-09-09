package helpers;

import javafx.scene.image.Image;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Stream;

public class imageLoader {
    public final ArrayList<Image> imageList = new ArrayList<>(); 
    private final String IMAGE_PATH = "res/";

    // Load image resources to imageList array
    public void loadImages(){
        try (Stream<Path> s = Files.walk(Paths.get(IMAGE_PATH))) {

            // File filtering
            Stream<Path> images = s.filter((Path p) -> {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return name.endsWith(".jpg");
            });

            // Add all image files to the array list
            images.forEach(i -> {
                String uri = i.toUri().toString();
                Image img = new Image(uri, true);
                imageList.add(img);
            });

            // Sort images in array so indexing is the same across systems
            imageList.sort((img1, img2) -> {
                String n1 = img1.getUrl();
                String n2 = img2.getUrl();
                return n1.compareToIgnoreCase(n2);
            });

        } catch (Exception e) { e.printStackTrace(); }
    }
}
