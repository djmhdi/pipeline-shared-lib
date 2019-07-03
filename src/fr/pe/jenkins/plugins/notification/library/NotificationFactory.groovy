package fr.pe.jenkins.plugins.notification.library

import fr.pe.jenkins.plugins.bundle.Bundle
import fr.pe.jenkins.plugins.notification.Notification
import fr.pe.jenkins.plugins.notification.NotificationDataKeys
import fr.pe.jenkins.plugins.notification.NotificationLevel
import fr.pe.jenkins.plugins.notification.NotificationProtocol

import java.util.logging.Logger

class NotificationFactory {

    private static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.html.NotificationFactory')


    static Notification createConstructionVersionNotif(String recipients, String recipientsCc, List protocols, def level,
                                                       String projectName) {
        Notification notification = ConstructionVersion.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        return notification
    }

    static Notification createConstructionVersionMail(String recipients, String recipientsCc, def level,
                                                      String projectName) {
        return createConstructionVersionNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName)
    }

    //-----------------------------------

    static Notification createConstructionSnapshotNotif(String recipients, String recipientsCc, List protocols, def level,
                                                       String projectName) {
        Notification notification = ConstructionSnapshot.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        return notification
    }

    static Notification createConstructionSnapshotMail(String recipients, String recipientsCc, def level,
                                                      String projectName) {
        return createConstructionSnapshotNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName)
    }

    //-----------------------------------

    static Notification createDemandeLivraisonNotif(String recipients, String recipientsCc, List protocols,
                                                    String projectName, String cadre, List<Bundle> bundles, String description, String matrice) {
        Notification notification =  DemandeLivraison.to(recipients, recipientsCc).by(protocols).on(NotificationLevel.SUCCESS)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        notification.appendData(NotificationDataKeys.CADRE.key, cadre)
        notification.appendData(NotificationDataKeys.BUNDLES.key, bundles)
        notification.appendData(NotificationDataKeys.DESCRIPTION.key, description)
        if (matrice) {
            notification.appendData(NotificationDataKeys.MATRICE.key, matrice)
        }
        return notification
    }

    static Notification createDemandeLivraisonNotif(String recipients, String recipientsCc, List protocols,
                                                    String projectName, String cadre, List<Bundle> bundles, String description) {
        return createDemandeLivraisonNotif(recipients, recipientsCc, protocols, projectName, cadre, bundles, description, null)
    }

    static Notification createDemandeLivraisonMail(String recipients, String recipientsCc,
                                                   String projectName, String cadre, List<Bundle> bundles, String description) {
        return createDemandeLivraisonNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], projectName, cadre, bundles, description)
    }

    //-----------------------------------

    static Notification createDeploiementNotif(String recipients, String recipientsCc, List protocols, def level,
                                               String projectName, String cadre) {
        Notification notification =  Deploiement.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        notification.appendData(NotificationDataKeys.CADRE.key, cadre)
        return notification
    }

    static Notification createDeploiementMail(String recipients, String recipientsCc, def level,
                                              String projectName, String cadre) {
        return createDeploiementNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName, cadre)
    }

    //-----------------------------------

    static Notification createLivraisonNotif(String recipients, String recipientsCc, List protocols, def level,
                                             String projectName, String cadre) {
        Notification notification =  Livraison.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        notification.appendData(NotificationDataKeys.CADRE.key, cadre)
        return notification
    }

    static Notification createLivraisonMail(String recipients, String recipientsCc, def level,
                                            String projectName, String cadre) {
        return createLivraisonNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName, cadre)
    }

    //-----------------------------------

    static Notification createPromotionNotif(String recipients, String recipientsCc, List protocols, def level,
                                              String projectName) {
        Notification notification = Promotion.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        return notification
    }

    static Notification createPromotionMail(String recipients, String recipientsCc, def level,
                                            String projectName) {
        return createPromotionNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName)
    }

    //-----------------------------------

    static Notification createVerificationDeploiementNotif(String recipients, String recipientsCc, List protocols, def level,
                                                           String projectName, String cadre) {
        Notification notification =  VerificationDeploiement.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        notification.appendData(NotificationDataKeys.CADRE.key, cadre)
        return notification
    }

    static Notification createVerificationDeploiementMail(String recipients, String recipientsCc, def level,
                                                          String projectName, String cadre) {
        return createVerificationDeploiementNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName, cadre)
    }

    //-----------------------------------

    static Notification createVerificationQualiteNotif(String recipients, String recipientsCc, List protocols, def level,
                                                       String projectName) {
        Notification notification =  VerificationQualite.to(recipients, recipientsCc).by(protocols).on(level)
        notification.appendData(NotificationDataKeys.PROJECT_NAME.key, projectName)
        return notification
    }
    static Notification createVerificationQualiteMail(String recipients, String recipientsCc, def level,
                                                      String projectName) {
        return createVerificationQualiteNotif(recipients, recipientsCc, [NotificationProtocol.MAIL], level, projectName)
    }

}