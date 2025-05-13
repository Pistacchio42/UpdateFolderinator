package org.example.parityfolder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.nio.file.StandardCopyOption.*;

public class HelloController {
    public Label data;
    public Label newerFolder;
    public VBox subVbox;
    public Label newerFolderDate;

    ArrayList<NamePath> pulsanti;
    NamePath newer = null;

    public void initialize() {
        try {
            FileInputStream fileIn = new FileInputStream("array.ser");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            pulsanti = (ArrayList<NamePath>) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            pulsanti = new ArrayList<>();
        }
        updateLabels();
        initializeButtons();
        searchNewer();
    }

    @FXML
    private void searchNewer() {
        ArrayList<LocalDateTime> attributiFiles = new ArrayList<>();
        String lastModifiedPath = "";
        LocalDateTime lastModifiedTime = LocalDateTime.of(1000, 1, 1, 1, 1);
        for (NamePath path : pulsanti) {
            File dir = new File(path.getParth());
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                break;
            }

            File lastModifiedFile = files[0];
            for (int i = 1; i < files.length; i++) {
                if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                    lastModifiedFile = files[i];
                }
            }
            BasicFileAttributes a = null;
            try {
                a = Files.readAttributes(lastModifiedFile.toPath(), BasicFileAttributes.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (lastModifiedTime.isBefore(a.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())) {
                lastModifiedTime = a.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                lastModifiedPath = path.getName();
                newer = path;
            }
            attributiFiles.add(a.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        }
        newerFolder.setText(lastModifiedPath);
        newerFolderDate.setText(lastModifiedTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    @FXML
    private void updateLabels() {
        try {
            FileInputStream fileIn = new FileInputStream("data.ser");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            LocalDateTime update = (LocalDateTime) objIn.readObject();
            data.setText(update.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (IOException | ClassNotFoundException e) {
            try {
                FileOutputStream fout = new FileOutputStream("data.ser");
                ObjectOutputStream out = new ObjectOutputStream(fout);
                out.writeObject(LocalDateTime.of(1000, 1, 1,1,1,1));
                out.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private void initializeButtons() {
        subVbox.getChildren().clear();
        for (NamePath pezzo : pulsanti) {
            System.out.println("adding: " + pezzo.getName());
            VBox col = new VBox();
            col.setAlignment(Pos.CENTER);
            col.setSpacing(2);
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setSpacing(4);
            Label lab = new Label(pezzo.getParth());
            TextField field = new TextField();
            field.setText(!pezzo.getName().equals("") ? pezzo.getName() : "");
            field.setPrefHeight(30);
            field.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("updating nome");
                    pezzo.setName(field.getText());
                    searchNewer();
                }
            });
            Button button = new Button("Scegli");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("updating label");
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Scegli la cartella da aggiungere alla lista");
                    chooser.setInitialDirectory(new File("C:\\"));
                    File directory = chooser.showDialog(new Stage());
                    if (directory != null) {
                        lab.setText(directory.toString());
                        pezzo.setParth(directory.toString());
                    }
                    searchNewer();
                }
            });
            row.getChildren().add(field);
            row.getChildren().add(button);
            col.getChildren().add(row);
            col.getChildren().add(lab);
            subVbox.getChildren().add(col);
        }
    }

    public void updateOlder(ActionEvent event) {
        for(NamePath search: pulsanti){
            if(!search.equals(newer)){
                delete(new File(search.getParth()));
                try {
                    copyDirectory(newer.getParth(), search.getParth());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("failed to copy");
                }
            }
        }
        LocalDateTime ora = LocalDateTime.now();
        try {
            FileOutputStream fout = new FileOutputStream("data.ser");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(ora);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data.setText(ora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cambiamenti");
        alert.setHeaderText("Ho aggiornato le cartelle vecchie");
        alert.show();
    }

    public void addNamePath(ActionEvent event) {
        pulsanti.add(new NamePath("", ""));
        initializeButtons();
    }

    public void addListener() {
        Window a = data.getScene().getWindow();
        a.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.out.println("closing operations");
                try {
                    cleanEmpty();
                    FileOutputStream fout = new FileOutputStream("array.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fout);
                    out.writeObject(pulsanti);
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void cleanEmpty() {
        int i = 0;
        while (i < pulsanti.size()) {
            if (pulsanti.get(i) == null || pulsanti.get(i).getParth().equals(""))
                pulsanti.remove(i);
            else i++;
        }
    }

    public void removeAll(ActionEvent event) {
        pulsanti.remove(pulsanti.size() - 1);
        searchNewer();
        initializeButtons();
    }

    public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
            throws IOException {
        Files.walk(Paths.get(sourceDirectoryLocation))
                .forEach(source -> {
                    Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                            .substring(sourceDirectoryLocation.length()));
                    try {
                        Files.copy(source, destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static boolean delete(File file) {
        File[] flist = null;
        if(file == null) return false;
        if (file.isFile()) return file.delete();
        if (!file.isDirectory()) return false;
        flist = file.listFiles();
        if (flist != null && flist.length > 0)
            for (File f : flist)
                if (!delete(f)) return false;
        return file.delete();
    }
}