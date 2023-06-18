package bootiful.ai;

import jakarta.annotation.Nonnull;
import org.mvnsearch.chatgpt.model.*;
import org.mvnsearch.chatgpt.model.function.GPTFunction;
import org.mvnsearch.chatgpt.model.function.GPTFunctionsStub;
import org.mvnsearch.chatgpt.model.function.JsonSchemaFunction;
import org.mvnsearch.chatgpt.model.function.Parameter;
import org.mvnsearch.chatgpt.spring.service.ChatGPTService;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Josh Long
 */
@SpringBootApplication
@RegisterReflectionForBinding({
        ChatCompletionRequest.class,
        ChatCompletionResponse.class,
        Parameter.class,
        GPTFunctionsStub.class ,
        GPTFunction .class ,
        ChatMessage.class,
        FunctionCall.class,
        JsonSchemaFunction.class,
        ChatCompletionChoice.class,
        ChatCompletionUsage.class
})
public class AiApplication {


    @Component
    @RegisterReflectionForBinding (MyGPTFunctions.SendEmailRequest.class)
    public static class MyGPTFunctions implements GPTFunctionsStub {

        public record SendEmailRequest(
                @Nonnull @Parameter("Recipients of email") List<String> recipients,
                @Nonnull @Parameter("Subject of email") String subject,
                @Parameter("Content of email") String content) {
        }

        @GPTFunction(name = "send_email", value = "Send email to receiver")
        public String sendEmail(SendEmailRequest request) {
            System.out.println("Recipients: " + String.join(",", request.recipients()));
            System.out.println("Subject: " + request.subject());
            System.out.println("Content:\n" + request.content());
            return "Email sent to " + String.join(",", request.recipients()) + " successfully!";
        }
    }


    @Bean
    ApplicationRunner runner(ChatGPTService service) {
        return args -> {
            var prompt = """ 
                        Would you please write an email to josh@joshlong.com and josh@coffeesoftware.com
                        and invite them to attend the production party on the next monday? Thanks in advance.
                    """
                    .trim();
            var request = ChatCompletionRequest.of(prompt);
            service
                    .chat(request)
                    .map(ChatCompletionResponse::getReplyCombinedText)
                    .subscribe(System.out::println);

            service
                    .chat(ChatCompletionRequest.functions(prompt,
                            List.of("send_email")))
                    .map(ChatCompletionResponse::getReplyCombinedText)
                    .subscribe(System.out::println);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

}

