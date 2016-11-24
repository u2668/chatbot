FROM java:8

COPY ./target/skype-chatbot-woodpecker.jar /opt

EXPOSE 8080

CMD ["java", "-jar", "/opt/skype-chatbot-woodpecker.jar", "--id=453a434b-7b36-4dfb-9731-28778a2ff40e", "--secret=UHqJPRv32HKR1LEffZjmJQd", "--gears=backend:8080", "--brain=brain:8080"]
