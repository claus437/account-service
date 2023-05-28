# Account Service

Built with java 11 and Maven 3.8.6

To run the application you will need an apache kafka on localhost:9082, or an 
alternative defined by -Dspring.kafka.bootstrap-servers

Kafka needs to be configured with a topic called "transactions" with message 
retention set to infinite.

Each running instance must define a unique group id for kafka, which is given
with the property -Daccount-service.instance.id

### The Flow

When the service receives a "deposits / withdraw" request, the transaction is 
sent to kafka and then pushed from kafka to all running account-service instances. 

Each instance will store the transaction in its own database and 
update the balance for the given account.

### User stories:
User story 1 : 
As a customer I want to create an account, so I can deposit/withdraw money to/from it. 
Acceptance criteria :
- User can create an account by entering an 8 digits number and an amount to deposit/withdraw. 
- Account is created in the system. 

User story 2 :
As a customer I want to deposit my money in the bank, so it is safe.
Acceptance criteria :
- user can enter desirable amount to deposit. 
- account balance is added by the amount deposited.

User story 3 :
As a customer I want to withdraw some of my money, so I can spend it.
Acceptance criteria :
- user can enter desirable amount to withdraw. 
- account balance is deducted by the amount withdrew. 

User story 4 :
As a customer I want to know my balance, so I know how much money I can spend or have spent.
Acceptance criteria :
- user can access and see the balance on their account.

User story 5 :
As a customer I want to know my latest transactions, so I can see what I have deposited or withdrew
Acceptance criteria :
- user can enter the number of the latest transactions (x) they would like to see.
- system returns a list of last x transactions. 
- user can see the list of last x transactions on their account. 
