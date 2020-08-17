package com.icici.apigw.util;

public class MailTemplete {
    public static String ResetPasswordSubject = "ICICI- Developer Portal: Password Reset";
    public static String ResetPassword = "<div style=\"line-height:21px;max-width:500px;\">" +
            "            Dear __FULL_NAME__," +
            "            <br />" +
            "            <br />" +
            "            Greetings from ICICI Bank." +
            "            <br /><br />" +
            "            The request for password reset has been received for your account. " +
            "            Please click on the link to reset your password for User __USERNAME__." +
            "            <br /><br />" +
            "            <a href=\"__RESET_LINK__\" target=\"_blank\">__RESET_LINK__</a>" +
            "            <br /><br />" +
            "            Note: This link will expire in 24 hours. Please ignore in case the password reset request was raised accidentally." +
            "            <br /><br />" +
            "            Sincerely," +
            "            <br />" +
            "            ICICI Bank Ltd." +
            "            <br /><br />" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. " +
            "       </div>";

    public static String NewRegistrationSubject = "ICICI- Developer Portal: New Registration";
    public static String NewRegistration_Old= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear __FULL_NAME__,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Greetings from ICICI Bank.\n" +
            "            <br /><br />\n" +
            "            Thank you for registering with ICICI Bank - Developer Portal. Your registration no __REG_NO__ is being reviewed. A separate notification would be sent to you on the status in 5 days.\n" +
            "            <br /><br />\n" +
            "            Sincerely,\n" +
            "            <br />\n" +
            "            ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    public static String NewRegistration_ToAPITEAMSubject = "ICICI- Developer Portal: New Registration Approval";
    public static String NewRegistration_ToAPITEAM= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear Team,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Following user requested for a registration." +
            "            <br /><br />\n" +
            "            Full Name:-  __FullName__ \n" +
            "            <br />\n" +
            "            Username:-  __Username__ \n" +
            "            <br />\n" +
            "            Email:-  __EMAIL__ \n" +
            "            <br />\n" +
            "            Contact Number:-  __Mobile__ \n" +
            "            <br />\n" +
            "            Domain:-  __Domain__ \n" +
            "            <br />\n" +
            "            Company:-  __Company__ \n" +
            "            <br />\n" +
            "            Company:-  __PartnerCode__ \n" +
            "            <br /><br />\n" +
            "            Please logon to approve or reject.\n" +
            "            <br /><br />\n" +
            "            Sincerely,\n" +
            "            <br />\n" +
            "            ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    
    public static String NewRegistration= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear __FULL_NAME__,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Greetings from ICICI Bank.\n" +
            "            <br /><br />\n" +
            "            Thank you for registering with ICICI Bank - Developer Portal. Your registration has been approved.\n" +
            "            <br /><br />\n" +
            "            Please reach out to our API Banking team on the email ID \"developersupport@icicibank.com\" for any assistance.\n" +
            "            <br /><br />\n" +
            "            Special offer: Now get promotional credit of Google Cloud Platform from our strategic partner Shivaami Cloud Services! For more details, please click <a href=\"https://www.shivaami.com/gcp-credit\">here</a>. T&C apply.\n" +
            "            <br /><br />\n" +
            "            Sincerely,\n" +
            "            <br />\n" +
            "            ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    
    public static String FEEDBACK_Subject = "ICICI- Developer Portal: Feedback";
    public static String USER_DETAILS_Subject = "ICICI- Developer Portal: User Details";
    public static String FEEDBACK= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear ApiTeam,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Feedback Details..\n" +
            "            <br /><br />" +
            "            Topic:-  __TOPIC__ \n" +
            "            <br /><br />\n" +
            "            Email:-  __EMAIL__ \n" +
            "            <br /><br />\n" +
            "            Location:-  __LOCATION__ \n" +
            "            <br /><br />\n" +
            "            Feedback:-  __FEEDBACK__ \n" +
            "            <br /><br />\n" +
            "            Sincerely,\n" +
            "            <br />\n" +
            "            ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    
    public static String USER_INTERESTED= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear ApiTeam,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            User Details..\n" +
            "            <br /><br />" +
            "            Full Name:-  __FULLNAME__ \n" +
            "            <br /><br />\n" +
            "            Email:-  __EMAIL__ \n" +
            "            <br /><br />\n" +
            "            Contact Number:-  __CONTACTNUMBER__ \n" +
            "            <br /><br />\n" +
            "            Location:-  __LOCATION__ \n" +
            "            <br /><br />\n" +
            "            Company:-  __COMPANY__ \n" +
            "            <br /><br />\n" +
            "            Requirments:-  __REQ__ \n" +
            "            <br /><br />\n" +
            "            Sincerely,\n" +
            "            <br />\n" +
            "            ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    
    public static String USER_MAIL_FEEDBACK_SUBJECT = "ICICI API Banking || Feedback Mail";
    public static String USER_MAIL_INTERESTED_SUBJECT = "ICICI API Banking || Collaboration Mail";
    public static String USER_INTERESTED_MAIL = "Dear __USERNAME__,\r\n" + 
    		"            <br /><br />\n" + 
    		"Greetings from ICICI Bank!!\r\n" + 
    		"            <br />\n" + 
    		"Thank you for showing interest in ICICI Bank API Products.\r\n" + 
    		"            <br />\n" +
    		"This automated reply is to let you know that we received your message and team would get back to you with a response as quickly as possible. \r\n" + 
    		"            <br /><br />\n" + 
    		"To have a smooth and seamless integration with you, it is imperative for us to have the following details-\r\n" +
    		"            <br />\n" +
    		"	1.       What are the products/services you are providing?\r\n" + 
    		"            <br />\n" +
    		"	2.       How do you want to integrate with ICICI bank?\r\n" + 
    		"            <br />\n" +
    		"	3.       What APIs would you require?\r\n" + 
    		"            <br />\n" +
    		"	4.       What is the market sector you are targeting?\r\n" + 
    		"            <br /><br />\n" +
    		"If you have a general question about using APIs, the working or the process, you&#39;re welcome to browse __SERVERURL__ for all products and the APIs .\r\n" + 
    		"            <br /><br />\n" +
    		"You can get connected to our team at __MAILID__.\r\n" + 
    		"            <br /><br />\n" + 
    		"Thanks and Regards,\r\n" + 
    		"            <br />\n" +
    		"API Banking Team\r\n" + 
    		"            <br />\n" +
    		"ICICI Bank";
    
    public static String USER_FEEDBACK_MAIL = "Dear __USERNAME__,\r\n" + 
    		"            <br /><br />\n" + 
    		"Greetings from ICICI Bank!!\r\n" +
    		"            <br />\n" + 
    		"This automated reply is to let you know that we received your message and the team would get back to you with a response as quickly as possible. \r\n" + 
    		"            <br /><br />\n" + 
    		"Thanks for the feedback on your experience with our API Banking Portal. We sincerely appreciate your insight as it helps us build a better customer experience.\r\n" +
    		"            <br />\n" +
    		"If you have any additional information that you think will help us to assist you, please feel free to reply to this email.\r\n" + 
    		"            <br /><br />\n" + 
    		"If you have a general question about using APIs, the working or the process, you&#39;re welcome to browse __SERVERURL__ for all products and the APIs .\r\n" +
    		"            <br />\n" +
    		"You can get connected to our team at __MAILID__.\r\n" + 
    		"            <br /><br />\n" + 
    		"Thanks and Regards,\r\n" + 
    		"            <br />\n" +
    		"API Banking Team\r\n" +
    		"            <br />\n" +
    		"ICICI Bank";
   /*================================================Appathon Registreation Mail================================================================================================================*/ 
    public static String AppathonMailSubject = "ICICI- Developer Portal: Appathon Registration";
    public static String AppathonMail= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear Team __TEAM_NAME__,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Greetings from ICICI Bank.\n" +
            "            <br /><br />\n" +
            "            Thank you for participating in ICICI Appathon!  a journey with ICICI Bank to create next gen solutions and scale them together\n" +
            "            <br /><br />\n" +
            "            You have crossed the first step, now it is time to start exploring the APIs available in the developer portal.\r\n" + 
            "			 We urge you to go through the portal and ideate on making best use of 250+ APIs that we have to create/enhance your solution.\n" +
            "            <br /><br />\n" +
            " 			 The team details are following:-<br/>\n"+
            " 			 Team :  __TEAM_NAME__  <br>\n"+
            " 			 Team Captain : __TEAM_CAPTAIN_NAME__ <br/>\n"+
            "            <br />\n" +
            "			 It's time to share your solution concept/idea, and visualize it with ICICI Bank APIs.\r\n" + 
            "			 <br /><br />\n" + 
            "			 To submit the idea before Feb 26, you need to follow below steps\r\n" + 
            "            <br /><br />\n" + 
            "			 Step 1: Login to developer portal using Appathon credentials\r\n" + 
            "            <br /><br />\n" + 
            "			 Step 2: Under your name, you will find Appathon dashboard\r\n" + 
            "            <br /><br />\n" + 
            "			 Step 3: You will find download template under Appathon Dashboard\r\n" + 
            "            <br /><br />\n" + 
            "			 Step 4: Time to fill the template with the details of your solution\r\n" + 
            "            <br /><br />\n" + 
            "		 	 Step 5: upload the PPT at the same location\r\n" + 
            "            <br /><br />\n" + 
            "			 We will announce the shortlisted participants shortly after the deadline. Shortlisted participants can use the APIs and develop a prototype."+       
            "            <br /><br />\n" +
            "			 Wish you all the best and hoping to create great products and solutions with you.\n"+
            "            <br /><br />\n" +
            "			 Note : For Security reasons, the username cannot be changed and password can only be changed using forgot password option which would be communicated on Team Captain's mail.\n"+
            "            <br /><br />\n" + 
            " 			 Please logon to ICICI Developer Portal at \"https://developer.icicibank.com\" for further details.\n <br>"+
            "            <br /><br />\n" +
            "            Please reach out to our ICICI Appathon team at \"iciciappathon@icicibank.com\" or \"appathontech@icicibank.com\" for any assistance.\n" +
            "            <br /><br />\n" +
            "            Sincerely,<br />\n" +
            "            Team ICICI Appathon, <br />\n ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    /*=================================================================================================================================================================*/ 

    /*==============================================================Appathon Update Details mail==================================================================================================*/ 
    public static String UpdateAppathonMailSubject = "ICICI- Developer Portal:Team Details Updated";
    public static String UpdateAppathonMail= "<div style=\"line-height:21px;max-width:500px;\">\n" +
            "            Dear Team __TEAM_NAME__,\n" +
            "            <br />\n" +
            "            <br />\n" +
            "            Greetings from ICICI Bank.\n" +
            "            <br /><br />\n" +
            "            This mail is to inform you that the updates made to your profile has been succesfully registered." +
            "            <br /><br />\n" +
            "            Incase you didn't make any changes please go to the \"https://developer.icicibank.com\" and change the password using forgot password in login option. " +
            "            <br /><br />\n" +
            "            Note : For Security reasons, the username cannot be changed and password can only be changed using forgot password option which would be communicated on Team Captain's mail. " +
            "            <br /><br />\n" +
            "			 Wish you all the best and hoping to create great products and solutions with you.\n"+
            "            <br /><br />\n" +
            " 			 Please logon to ICICI Developer Portal at \"https://developer.icicibank.com\" for further details.\n <br>"+
            "            <br /><br />\n" +
            "            Please reach out to our ICICI Appathon team at \"iciciappathon@icicibank.com\" or \"appathontech@icicibank.com\" for any assistance.\n" +
            "            <br /><br />\n" +
            "            Sincerely,<br />\n" +
            "            Team ICICI Appathon, <br />\n ICICI Bank Ltd.\n" +
            "            <br /><br />\n" +
            "            This is a system-generated e-mail. Please do not reply to this e-mail. \n" +
            "       </div>";
    /*==============================================================Appathon Update Details mail==================================================================================================*/ 

    
}

