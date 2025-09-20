package com.example.buspricing.util;

import com.example.buspricing.controller.request.Passenger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
public class PriceDescriptionFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public String passengerDescription(int index, Passenger passenger) {
        return String.format("Passenger %d (%s)", index, passenger.getType().name());
    }

    public String passengerPriceDescription(Passenger passenger, BigDecimal base, BigDecimal withTax, BigDecimal taxPercentSum) {
        if (passenger.getType() == Passenger.Type.ADULT) {
            return String.format("Adult (%s EUR + %s%%) = %s EUR",
                    format(base),
                    taxPercentSum.stripTrailingZeros().toPlainString(),
                    format(withTax));
        } else {
            return String.format("Child (%s EUR x 50%% + %s%%) = %s EUR",
                    format(base),
                    taxPercentSum.stripTrailingZeros().toPlainString(),
                    format(withTax));
        }
    }

    public String luggageDescription(int passengerIndex, int count) {
        return String.format("Luggage for passenger %d (%s)", passengerIndex, LuggageDescriptionUtil.describe(count));
    }

    public String luggagePriceDescription(int count, BigDecimal base, BigDecimal withTax, BigDecimal taxPercentSum) {
        return String.format("%s (%d x %s EUR x 30%% + %s%%) = %s EUR",
                LuggageDescriptionUtil.describe(count),
                count,
                format(base),
                taxPercentSum.stripTrailingZeros().toPlainString(),
                format(withTax));
    }

    public String totalDescription(BigDecimal total) {
        return format(total) + " EUR";
    }

    private String format(BigDecimal value) {
        return DECIMAL_FORMAT.format(value);
    }
}
