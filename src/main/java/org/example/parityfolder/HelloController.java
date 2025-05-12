package org.example.parityfolder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;

public class HelloController {
    public Label data;
    public Label newerFolder;
    public VBox subVbox;

    ArrayList<NamePath>pulsanti;

    public void initialize(){
        try {
            FileInputStream fileIn = new FileInputStream("array.ser");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            pulsanti= (ArrayList<NamePath>) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            pulsanti= new ArrayList<>();
        }
        updateLabels();
        initializeButtons();
        searchNewer();
    }

    private void searchNewer() {
        ArrayList<LocalDateTime> attributiFiles= new ArrayList<>();
        for(NamePath path : pulsanti){
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
            BasicFileAttributes a=null;
            try {
                a =Files.readAttributes(lastModifiedFile.toPath(),BasicFileAttributes.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            attributiFiles.add(a.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        }
    }

    private void updateLabels() {
        try {
            FileInputStream fileIn = new FileInputStream("data.ser");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            LocalDate update= (LocalDate) objIn.readObject();
            data.setText( update.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (IOException | ClassNotFoundException e) {
            try {
                FileOutputStream fout= new FileOutputStream("data.ser");
                ObjectOutputStream out = new ObjectOutputStream(fout);
                out.writeObject(LocalDate.of(1000,1,1));
                out.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private void initializeButtons() {
        subVbox. getChildren().clear();
        for(NamePath pezzo:pulsanti) {
            System.out.println("adding: "+pezzo.getName());
            VBox col = new VBox();
            col.setAlignment(Pos.CENTER);
            col.setSpacing(2);
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setSpacing(4);
            Label lab=new Label(pezzo.getParth());
            TextField field = new TextField();
            field.setText(!pezzo.getName().equals("")?pezzo.getName():"");
            field.setPrefHeight(30);
            field.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("updating nome");
                    pezzo.setName(field.getText());
                }
            });
            Button button= new Button("Scegli");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("updating label");
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Scegli la cartella da aggiungere alla lista");
                    chooser.setInitialDirectory(new File("C:\\"));
                    File directory= chooser.showDialog(new Stage());
                    if (directory!= null ) {
                        lab.setText(directory.toString());
                        pezzo.setParth(directory.toString());
                    }
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


        LocalDate ora= LocalDate.now();
        try {
            FileOutputStream fout= new FileOutputStream("data.ser");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(ora);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data.setText(ora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    public void addNamePath(ActionEvent event) {
        pulsanti.add(new NamePath("",""));
        initializeButtons();
    }

    public void addListener() {
        Window a = data.getScene().getWindow();
        a.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.out.println("closing operations");
                try{
                    FileOutputStream fout= new FileOutputStream("array.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fout);
                    out.writeObject(pulsanti);
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void removeAll(ActionEvent event) {
        pulsanti.clear();
        initializeButtons();
    }
}