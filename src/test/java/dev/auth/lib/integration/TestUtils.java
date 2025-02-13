package dev.auth.lib.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJsonFromFile(String file) throws IOException {
        File expectedFile = ResourceUtils.getFile("classpath:" + file);
        return new String(Files.readAllBytes(expectedFile.toPath()));
    }

    public static String getStringFromFile(String file) throws IOException {
        File expectedFile = ResourceUtils.getFile("classpath:" + file);
        return new String(Files.readAllBytes(expectedFile.toPath()));
    }

    /**
     * Método para verificar que se ha recibido un email en el buzón de las pruebas.
     * Para poder usar este método se tiene que tener en consideración el siguiente funcionamiento.
     * <li>El método comprueba que solo se ha recibido un correo.</li>
     * <li>El contenido del email tiene que estar encerrado entre | en la plantilla. Se hace así para facilitar le parseo del mensaje.</li>
     *
     * @param greenMail La clase que simula el receptor de emails.
     * @param message El mensaje que se quiere verificar.
     * @throws MessagingException Cualquier error que se produzca en el "parseo" del mensaje.
     */
    public static void verifyEmailMessage(GreenMailExtension greenMail, TestMessage message) throws MessagingException {
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage receivedMessage = messages[0];
        assertEquals(1, receivedMessage.getAllRecipients().length);
        assertEquals(message.getTo(), receivedMessage.getAllRecipients()[0].toString());
        assertEquals(message.getFrom(), receivedMessage.getFrom()[0].toString());
        assertEquals(message.getSubject(), receivedMessage.getSubject());
        String body = GreenMailUtil.getBody(messages[0]);
        String[] test = body.split("\\|");
        assertEquals(message.getContent(), test[1]);
    }

    @Builder
    @Getter
    public static class TestMessage {
        private String to;
        private String from;
        private String subject;
        private String content;
    }
}
