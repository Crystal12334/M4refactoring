package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private static final int TRAGEDY_BASE_AMOUNT = 40000;
    private static final int COMEDY_BASE_AMOUNT = 30000;
    private static final int EXTRA_CHARGE = 1000;
    private static final int AUDIENCE_THRESHOLD = 30;
    private static final int VOLUME_CREDIT_THRESHOLD = 100;
    private static final int COMEDY_OVER_BASE_AMOUNT = 1000;
    private static final int COMEDY_EXTRA_PER_PERSON = 500;
    private static final int COMEDY_PER_AUDIENCE_AMOUNT = 300;
    private static final int COMEDY_EXTRA_VOLUME_DIVISOR = 5;
    private static final int AMOUNT_DIVISOR = 100;

    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder("Statement for "
                + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());

            int thisAmount = 0;
            switch (play.getType()) {
                case "tragedy":
                    thisAmount = TRAGEDY_BASE_AMOUNT;
                    if (p.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                        thisAmount += EXTRA_CHARGE * (p.getAudience() - AUDIENCE_THRESHOLD);
                    }
                    break;
                case "comedy":
                    thisAmount = Constants.COMEDY_BASE_AMOUNT;
                    if (p.getAudience() > AUDIENCE_THRESHOLD) {
                        thisAmount += COMEDY_BASE_AMOUNT
                                + COMEDY_EXTRA_PER_PERSON
                                * (p.getAudience() - AUDIENCE_THRESHOLD);
                    }
                    thisAmount += COMEDY_PER_AUDIENCE_AMOUNT * p.getAudience();
                    break;
                default:
                    throw new RuntimeException(String.format("unknown type: %s", play.getType()));
            }

            // add volume credits
            volumeCredits += Math.max(p.getAudience() - VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(play.getType())) {
                volumeCredits += p.getAudience() / COMEDY_EXTRA_VOLUME_DIVISOR;
            }

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", play.getName(),
                    frmt.format(thisAmount / AMOUNT_DIVISOR), p.getAudience()));
            totalAmount += thisAmount;
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / AMOUNT_DIVISOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }
}
