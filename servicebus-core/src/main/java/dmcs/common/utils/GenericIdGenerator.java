package dmcs.common.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import java.text.DecimalFormat;
import java.util.Calendar;

public final class GenericIdGenerator {

    private static final EthernetAddress nic = EthernetAddress.fromInterface();
    private static final TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(nic);

    private GenericIdGenerator() {
    }

    public static String getId() {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_YEAR);

        return String.format("%s%s%s",
                year,
                new DecimalFormat("000").format(day),
                uuidGenerator.generate().toString().replace("-", ""));
    }

}
