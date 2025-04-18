package wanted.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static wanted.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static wanted.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static wanted.logic.parser.CliSyntax.PREFIX_PHONE;
import static wanted.testutil.Assert.assertThrows;
import static wanted.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static wanted.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import wanted.commons.core.datatypes.MoneyInt;
import wanted.logic.commands.AddCommand;
import wanted.logic.commands.ClearCommand;
import wanted.logic.commands.CommandTestUtil;
import wanted.logic.commands.DeleteCommand;
import wanted.logic.commands.DelhistCommand;
import wanted.logic.commands.EdithistCommand;
import wanted.logic.commands.EdithistCommand.EditTransactionDescriptor;
import wanted.logic.commands.ExitCommand;
import wanted.logic.commands.FindCommand;
import wanted.logic.commands.HelpCommand;
import wanted.logic.commands.IncreaseCommand;
import wanted.logic.commands.ListCommand;
import wanted.logic.commands.PhoneCommand;
import wanted.logic.commands.RenameCommand;
import wanted.logic.commands.TagCommand;
import wanted.logic.parser.exceptions.ParseException;
import wanted.model.loan.Loan;
import wanted.model.loan.LoanDate;
import wanted.model.loan.NameContainsKeywordsPredicate;
import wanted.model.loan.Phone;
import wanted.testutil.PersonBuilder;
import wanted.testutil.PersonUtil;

public class LoanBookParserTest {

    private final LoanBookParser parser = new LoanBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        // TODO: Date field needs to be cleaned out from all the tests involving Loan itself
        Loan person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_increase() throws Exception {
        assertTrue(parser.parseCommand(IncreaseCommand.COMMAND_WORD
                + " 1 l/10.10 d/2024-09-09") instanceof IncreaseCommand);
        assertTrue(parser.parseCommand(IncreaseCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()
                + " " + "l/" + CommandTestUtil.VALID_AMOUNT_AMY
                + " " + " d/" + CommandTestUtil.VALID_DATE_AMY) instanceof IncreaseCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = Arrays.asList("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_delhist() throws Exception {
        String command = DelhistCommand.COMMAND_WORD + " 1 i/2";
        assertEquals(new DelhistCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), parser.parseCommand(command));
    }

    @Test
    public void parseCommand_phone() throws Exception {
        String command = PhoneCommand.COMMAND_WORD + " 1 " + PREFIX_PHONE + CommandTestUtil.VALID_PHONE;
        assertEquals(new PhoneCommand(INDEX_FIRST_PERSON, new Phone(CommandTestUtil.VALID_PHONE)),
                parser.parseCommand(command));
    }

    @Test
    public void parseCommand_rename() throws Exception {
        assertTrue(parser.parseCommand(RenameCommand.COMMAND_WORD + " 1 n/Julian") instanceof RenameCommand);
        assertTrue(parser.parseCommand(RenameCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()
                + " " + "n/" + CommandTestUtil.VALID_NAME_AMY) instanceof RenameCommand);
    }

    @Test
    public void parseCommand_tag() throws Exception {
        assertTrue(parser.parseCommand(TagCommand.COMMAND_WORD + " 1 t/friend") instanceof TagCommand);
        assertTrue(parser.parseCommand(TagCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()
                + " " + "t/" + CommandTestUtil.VALID_TAG_FRIEND) instanceof TagCommand);
    }

    @Test
    public void parseCommand_edithist() throws Exception {
        String command = EdithistCommand.COMMAND_WORD + " 1 i/2 l/20.25 d/1111-01-01";
        EditTransactionDescriptor expectedDescriptor = new EditTransactionDescriptor();
        expectedDescriptor.setAmount(MoneyInt.fromCent(2025));
        expectedDescriptor.setDate(new LoanDate("1111-01-01"));
        EdithistCommand expected = new EdithistCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                expectedDescriptor);
        assertEquals(expected, parser.parseCommand(command));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
                -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
