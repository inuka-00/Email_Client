// Inuka Ampavila
// 200036T

//import libraries


import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.*;

public class Email_Client {
    //facade pattern
    public static void main(String[] args) throws AddressException, IOException, ClassNotFoundException {
        //initialize Email.EmailMachine and RecipientManger
        var emailMachine = new EmailMachine();
        var recipientManager = new RecipientManager();

        //Wishing for bdays
        LocalDate today = LocalDate.now();


        System.out.println("Sending mails for birthday wishes....");
        if (recipientManager.getBdays(today) == null) {
            System.out.println("No bdays today");
        } else {

            for (var person : recipientManager.getBdays(today)) {
                //only send mails to persons not wished already during the current day
                if (!emailMachine.checkWishSent(person)) {
                    Email wish_mail = person.wish();
                    emailMachine.sendMail(wish_mail);
                }

            }

        }

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nEnter option type: \n"
                    + "1 - Adding a new recipient\n"
                    + "2 - Sending an email\n"
                    + "3 - Printing out all the recipients who have birthdays\n"
                    + "4 - Printing out details of all the emails sent\n"
                    + "5 - Printing out the number of recipient objects in the application\n"
                    + "6 - Exit Program");

            int option = 0;

            try {
                option = scanner.nextInt();


            } catch (InputMismatchException e) {
                System.out.println("Enter an integer from 1 to 6");
            }

            //scanner.close();

            switch (option) {
                case 1:
                    // input format - RecipientClasses.Official: nimal,nimal@gmail.com,ceo
                    System.out.println("Input Recipient Details: ");
                    // Use a single input to get all the details of a recipient
                    Scanner user_in = new Scanner(System.in);
                    String data = user_in.nextLine();
                    //user_in.close();

                    // code to add a new recipient
                    //recipient manager will handle registering new recipient
                    recipientManager.addRecipient(data, true);

                    break;
                case 2:
                    // input format - email, subject, content
                    System.out.println("Enter input in the form; email,subject,body");
                    Scanner user_input = new Scanner(System.in);
                    String email_data = user_input.nextLine();
                    //user_input.close();
                    emailMachine.sendMail(email_data);
                    // code to send an email
                    break;
                case 3:
                    // input format - yyyy/MM/dd (ex: 2018/09/17)
                    System.out.println("Enter Date: yyyy/mm/dd");
                    Scanner get_date = new Scanner(System.in);
                    String date = get_date.nextLine();
                    //get_date.close();


                    // code to print recipients who have birthdays on the given date
                    //null error fix
                    if (recipientManager.getBdays(date) == null) {
                        System.out.println("No Bdays on this date.");

                    } else {
                        for (Wishable person : recipientManager.getBdays(date)) {
                            System.out.println("Happy Birthday " + person.getName());
                        }
                    }
                    break;
                case 4:
                    // input format - yyyy/MM/dd (ex: 2018/09/17)
                    Scanner getDate = new Scanner(System.in);
                    String date_needed = getDate.nextLine();


                    // code to print the details of all the emails sent on the input date
                    emailMachine.sentMail(date_needed);
                    break;
                case 5:
                    // code to print the number of recipient objects in the application
                    System.out.println("Recipient Count: ");
                    System.out.println(recipientManager.getSize());
                    break;
                case 6:
                    //exit program
                    System.out.println("Exit Program");
                    emailMachine.switchoff();
                    return;

            }
        }

    }
}

// create more classes needed for the implementation (remove the  public access modifier from classes when you submit your code)

//Utility Classes
class DateVerifier {
    public boolean verify(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);

        } catch (Exception e) {
            System.out.println("Invalid Date.");
            return false;
        }

        return true;
    }
}

class EmailVerifier {
    public boolean isValidMail(String mail) {
        boolean is_valid = true;

        try {
            InternetAddress validAddress = new InternetAddress(mail, true);
        } catch (AddressException e) {
            System.out.println("Invalid Email.");
            is_valid = false;
        }

        return is_valid;
    }
}

//Recipient Handling

//Recipient Classes
abstract class Recipient {
    protected String name;
    protected InternetAddress email;
    private static int count = 0;

    Recipient(String name, String email) throws AddressException {
        this.email = new InternetAddress(email);
        this.name = name;
        count++;
    }

    public static int getCount() {
        return count;
    }

    public InternetAddress getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }
}

interface Wishable {
    Email wish();

    MonthDay getbday();

    String getName();

    InternetAddress getEmail();
}

class Personal extends Recipient implements Wishable {

    private LocalDate bday;
    private String nickname;

    public Personal(String name, String nickname, String email, LocalDate bday) throws AddressException {
        super(name, email);
        this.nickname = nickname;
        this.bday = bday;
    }

    public Email wish() {
        String wish = "Hugs and Love on your Birthday " + this.name + "\n" + "-Inuka";
        Email bday_wish = new Email(this.email, "Happy Birthday", wish);
        return bday_wish;
    }

    public MonthDay getbday() {
        return MonthDay.from(this.bday);
    }

}

class Official extends Recipient {
    protected String designation;

    public Official(String name, String email, String designation) throws AddressException {
        super(name, email);
        this.designation = designation;
    }


}

class OfficeFriend extends Official implements Wishable {
    //private Birthday
    private LocalDate bday;

    public OfficeFriend(String name, String email, String designation, LocalDate bday) throws AddressException {
        super(name, email, designation);
        //code to get birthday
        this.bday = bday;
    }

    public Email wish() {
        String wish = "Wish you a very Happy Birthday " + this.name + "\n" + "-Inuka";
        Email bday_wish = new Email(this.email, "Happy Birthday", wish);
        return bday_wish;
    }

    public MonthDay getbday() {
        return MonthDay.from(this.bday);
    }
}

//Builders
interface RecipientBuilder {
    public Recipient create(String[] inputs) throws AddressException;
}

class PersonalBuilder implements RecipientBuilder {
    private EmailVerifier emailVerifier = new EmailVerifier();
    private DateVerifier dateVerifier = new DateVerifier();

    public Recipient create(String[] recipientData) throws AddressException {
        //format - name,nickname,email,bday
        String name, nickname, mail, bday;

        try {
            name = recipientData[0].strip();
            nickname = recipientData[1].strip();
            mail = recipientData[2].strip();
            bday = recipientData[3].strip();
            bday = bday.replace('/', '-');
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("ERROR: Please enter input in the correct format.");
            return null;
        }

        LocalDate BdayDate;
        //check if date is valid
        if (dateVerifier.verify(bday)) {
            BdayDate = LocalDate.parse(bday);
        } else {
            return null;
        }

        if (emailVerifier.isValidMail(mail)) {
            return new Personal(name, nickname, mail, BdayDate);

        } else {
            System.out.println("Invalid email");
            return null;
        }
    }
}

class OfficialBuilder implements RecipientBuilder {
    private EmailVerifier emailVerifier = new EmailVerifier();

    public Recipient create(String[] recipientData) throws AddressException {
        //format name,email,desigantion
        String name, mail, designation;
        try {
            name = recipientData[0].strip();
            mail = recipientData[1].strip();
            designation = recipientData[2].strip();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("ERROR: Please enter input in proper format.");
            return null;
        }
        if (emailVerifier.isValidMail(mail)) {
            return new Official(name, mail, designation);
        } else {
            System.out.println("Invalid email");
            return null;
        }

    }
}

class OfficeFriendBuilder implements RecipientBuilder {
    private EmailVerifier emailVerifier = new EmailVerifier();
    private DateVerifier dateVerifier = new DateVerifier();

    public Recipient create(String[] recipientData) throws AddressException {
        //format name,email,designation,birthday
        String name, mail, designation, bday;
        try {
            name = recipientData[0].strip();
            mail = recipientData[1].strip();
            designation = recipientData[2].strip();
            bday = recipientData[3].strip();
            bday = bday.replace('/', '-');
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("ERROR: Please enter input in the proper format.");
            return null;
        }


        LocalDate BdayDate;
        //check if date is valid
        if (dateVerifier.verify(bday)) {
            BdayDate = LocalDate.parse(bday);
        } else {
            return null;
        }

        if (emailVerifier.isValidMail(mail)) {
            return new OfficeFriend(name, mail, designation, BdayDate);
        } else {
            System.out.println("Invalid email");

            return null;
        }
    }
}

class RecipientManager {
    private EmailVerifier emailVerifier = new EmailVerifier();
    private TextReader fileReader = new TextReader("clientList.txt");
    private TextWriter fileWriter = new TextWriter("clientList.txt");

    //Recipient Objects Maintained in the program
    //The Hashmap ensures that same email is not duplicated
    HashMap<InternetAddress, Recipient> recipients = new HashMap<>();

    //Birthday Data
    //Saves wishable recipients seperately grouped by month and day of their bday
    HashMap<MonthDay, ArrayList<Wishable>> bdayData = new HashMap<>();

    //Read from list in text file when initialized (Constructor)
    public RecipientManager() throws AddressException {
        fileReader.fileRead();
        for (var i : fileReader.getInputs()) {
            addRecipient(i, false);
        }
    }

    //Builder Pattern
    public void addRecipient(String data, boolean from_user) throws AddressException {
        String[] data_arr = data.split(":", 2);
        String type;
        String[] recipientData;
        try {
            type = data_arr[0].strip().toLowerCase();
            recipientData = data_arr[1].split(",", 10);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("ERROR: Please check input format and try again.");
            return;
        }
        Recipient recipient;
        RecipientBuilder builder = null;
        switch (type) {
            case "official": {
                builder = new OfficialBuilder();
                break;
            }
            case "office_friend": {
                builder = new OfficeFriendBuilder();
                break;
            }
            case "personal": {
                builder = new PersonalBuilder();

                break;
            }

            default:
                System.out.println("ERROR: Invalid Recipient Type" + " " + type);
                return;
        }

        recipient = builder.create(recipientData);

        if (recipient == null) {
            System.out.println("Error Occurred. Please check input and try again.");
            return;
        }

        //The Hashmap ensures that same email is not duplicated
        //It does not take in recipients with already registered email addresses
        if (recipients.get(recipient.getEmail()) != null) {
            System.out.println(recipient.getEmail() + ":" + "A recipient with this email address already exists.");
            return;
        }
        recipients.put(recipient.getEmail(), recipient);

        //if this is a new recipient from user we need to add to the ouput text
        if (from_user) {
            fileWriter.fileWrite(data);
        }

        //If the recipient is a wishable object add to the bday hashmap
        if (recipient instanceof Wishable) {
            MonthDay md = ((Wishable) recipient).getbday();

            if (bdayData.get(md) == null) {
                var array = new ArrayList<Wishable>();
                array.add((Wishable) recipient);
                bdayData.put(md, array);
            } else {
                bdayData.get(md).add((Wishable) recipient);
            }
        }

    }

    public int getSize() {
        return Recipient.getCount();
    }

    //string input
    public ArrayList<Wishable> getBdays(String date) {
        date = date.replace("/", "-");
        try {
            MonthDay md = MonthDay.from(LocalDate.parse(date));
            return bdayData.get(md);
        } catch (Exception e) {
            System.out.println("Invalid Date");
            return null;
        }

    }

    //date input
    public ArrayList<Wishable> getBdays(LocalDate date) {

        MonthDay md = MonthDay.from(date);
        return bdayData.get(md);
    }

}

//Email Management

class Email implements Serializable {
    private InternetAddress recipient;   //the email address of the receiver
    private String body;
    private String subject;
    private LocalDate sent_date;

    public Email(InternetAddress recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.sent_date = LocalDate.now();
    }

    public InternetAddress getRecipient() {
        return recipient;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDate getSent_date() {
        return sent_date;
    }
}

class EmailLogs {
    private EmailDeserializer emailDeserializer = new EmailDeserializer("elogs.xer");
    private EmailSerializer emailSerializer = new EmailSerializer("elogs.xer");
    private HashMap<LocalDate, ArrayList<Email>> logs;

    private static EmailLogs instance = null;

    private EmailLogs() throws IOException, ClassNotFoundException {
        //initialize logs
        Object deSerialized = emailDeserializer.deSerialize();

        if (deSerialized == null) {
            //if no previously initialized object or file then insert a new hashMap;
            System.out.println("Initialized new logs.");
            logs = new HashMap<>();
        } else {
            this.logs = (HashMap<LocalDate, ArrayList<Email>>) deSerialized;
        }

    }

    public static EmailLogs getInstance() throws IOException, ClassNotFoundException {
        if (instance == null) {
            instance = new EmailLogs();
            return instance;
        } else {
            return instance;
        }
    }

    public void update(Email email) {
        if (logs.get(email.getSent_date()) != null) {
            logs.get(email.getSent_date()).add(email);
        } else {
            ArrayList<Email> new_element = new ArrayList<>();
            new_element.add(email);
            logs.put(email.getSent_date(), new_element);
        }
    }

    //method to output emails sent on a given date
    public ArrayList<Email> mailFromDate(LocalDate date) {
        return logs.get(date);
    }

    //saves logs in file as serialized form
    public void saveLogs() throws IOException {
        emailSerializer.serialize(logs);
    }

    public HashMap<LocalDate, ArrayList<Email>> getLogs() {
        return logs;
    }
}

class SendEmailTLS {
    /*A static method which takes an email and sends it to a previously verified address*/

    public void sendMail(Email email) {

        final String username = "example@gmail.com";
        final String password = "example";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ampavilatest@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("\n" +
                            email.getRecipient())
            );
            message.setSubject(email.getSubject());
            message.setText("Dear Sir/Madam,"
                    + "\n" + email.getBody());

            Transport.send(message);

            System.out.println("Done. Email Sent.");

        } catch (MessagingException e) {
            System.out.println("ERROR - Email did not send.");
            e.printStackTrace();
        }
    }


}

//Adapter Pattern
class UserInputHandler {
    private EmailVerifier mailVerifier = new EmailVerifier();

    //converts user input into mail format
    public Email convertToMail(String user_in) throws AddressException {
        Email new_mail = null;
        String[] array_str;

        array_str = user_in.split(",", 3);
        for (String a : array_str) {
            a = a.strip();
        }
        try {
            if (mailVerifier.isValidMail(array_str[0])) {
                //array_str index 0 - email , 1 - subject, 2 - body
                InternetAddress email_add = new InternetAddress(array_str[0]);
                new_mail = new Email(email_add, array_str[1], array_str[2]);

            } else {
                System.out.println("ERROR: Mail Address invalid. Please verify email and try again.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("ERROR: Please enter data in correct format.");
        }

        return new_mail;
    }
}

//this is a Mediator class
class EmailMachine {


    private UserInputHandler userInputHandler = new UserInputHandler();
    private SendEmailTLS sendEmailTLS = new SendEmailTLS();
    //Handles logging emails
    private EmailLogs emailLogs;

    //utility classes
    private DateVerifier dateVerifier = new DateVerifier();

    public EmailMachine() throws IOException, ClassNotFoundException {
        //initialize logs
        emailLogs = EmailLogs.getInstance();

    }

    //handles sending emails from direct user input
    public void sendMail(String user_in) throws AddressException {
        Email new_mail = userInputHandler.convertToMail(user_in);
        if (new_mail != null) {
            sendEmailTLS.sendMail(new_mail);
            //add_code to logs.
            emailLogs.update(new_mail);
        }
    }

    //overload to send emails when recipient is registered
    public void sendMail(Email mail) {
        sendEmailTLS.sendMail(mail);
        emailLogs.update(mail);
    }


    //get all emails sent on a given date through logs
    public void sentMail(String date) {

        date = date.replace("/", "-");
        if (dateVerifier.verify(date)) {
            ArrayList<Email> mailList = emailLogs.mailFromDate(LocalDate.parse(date));
            if (mailList == null) {
                System.out.println("No mails sent on this date");

            } else {
                for (var i : mailList) {
                    System.out.println("To: " + i.getRecipient());
                    System.out.println("Subject: " + i.getSubject());
                    System.out.println(i.getBody());
                    System.out.println("------------");
                }
            }
        }

    }

    //function to check whether a wish was sent to a particular person during the current day
    public boolean checkWishSent(Wishable person) {


        ArrayList<Email> sentToday = emailLogs.mailFromDate(LocalDate.now());

        if (sentToday != null) {
            for (var sentMail : sentToday) {

                if (sentMail.getRecipient().equals(person.getEmail()) && sentMail.getSubject().equals("Happy Birthday")) {
                    return true;
                }
            }
        }

        return false;
    }

    //switch off the machine by saving serialized objects etc.
    public void switchoff() throws IOException {
        emailLogs.saveLogs();
    }


}

//File Handling Classes
class EmailDeserializer {
    String fileName;

    public EmailDeserializer(String fileName) {
        this.fileName = fileName;
    }

    public Object deSerialize() {

        FileInputStream fileInputStream = null;
        ObjectInputStream os = null;
        try {
            fileInputStream = new FileInputStream(fileName);
            os = new ObjectInputStream(fileInputStream);
            return os.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}

class EmailSerializer {
    String fileName;

    public EmailSerializer(String fileName) {
        this.fileName = fileName;
    }

    public void serialize(Object object) {
        FileOutputStream fs = null;
        ObjectOutputStream os = null;
        try {
            fs = new FileOutputStream(fileName);
            os = new ObjectOutputStream(fs);
            os.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

class TextReader {
    private String fileName;
    private ArrayList<String> inputs = new ArrayList<>();

    public TextReader(String fileName) {
        this.fileName = fileName;
    }

    public void fileRead() {
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new java.io.FileReader(fileName);
            reader = new BufferedReader(fr);
            String line = null;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                //System.out.println(line);
                inputs.add(line);
            }
        } catch (FileNotFoundException fe) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error - IOException");
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getInputs() {
        return inputs;
    }
}

class TextWriter {
    private String fileName;

    public TextWriter(String fileName) {
        this.fileName = fileName;
    }

    public void fileWrite(String s) {
        FileWriter fs = null;
        BufferedWriter output = null;
        try {
            fs = new java.io.FileWriter(fileName, true);
            output = new BufferedWriter(fs);
            output.write(s);
            output.newLine();

        } catch (IOException except) {
            except.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (fs != null) {
                    fs.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
