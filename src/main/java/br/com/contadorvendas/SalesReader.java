package br.com.contadorvendas;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.*;

public class SalesReader {

    private final List<Sale> sales;

    public SalesReader(String salesFile) {

        final var dataStream = ClassLoader.getSystemResourceAsStream(salesFile);

        if (dataStream == null) {
            throw new IllegalStateException("File not found or is empty");
        }

        final var builder = new CsvToBeanBuilder<Sale>(new InputStreamReader(dataStream, StandardCharsets.ISO_8859_1));

        sales = builder
                .withType(Sale.class)
                .withSeparator(';')
                .build()
                .parse();
    }

    public void totalOfCompletedSales() {

        System.out.println("Total vendas completas R$: " +
                toCurrency(
                        sales.stream()
                                .filter(Sale::isCompleted)
                                .map(Sale::getValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
        );

        imprimiSeparador();

    }

    public void totalOfCancelledSales() {

        System.out.println("Total vendas canceladas R$: " +
                toCurrency(
                        sales.stream()
                                .filter(Sale::isCancelled)
                                .map(Sale::getValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
        );

        imprimiSeparador();

    }

    public void mostRecentCompletedSale() {

        sales.stream()
                .filter(Sale::isCompleted)
                .sorted(Comparator.comparing(Sale::getNumber).reversed())
                .max(Comparator.comparing(Sale::getSaleDate))
                .stream()
                .forEach(s -> System.out.println("Venda mais recente: " + s.getNumber()));

        imprimiSeparador();

    }

    public void daysBetweenFirstAndLastCancelledSale() {

        Optional<Sale> firstSale = sales.stream()
                .filter(Sale::isCompleted)
                .min(Comparator.comparing(Sale::getSaleDate));

        Optional<Sale> lastSale = sales.stream()
                .filter(Sale::isCompleted)
                .max(Comparator.comparing(Sale::getSaleDate));

        if (lastSale.isPresent() && firstSale.isPresent())
            System.out.println("Quantidade dias entre primeira e ultima venda: " +
                    ChronoUnit.DAYS.between(firstSale.get().getSaleDate(), lastSale.get().getSaleDate()));

        imprimiSeparador();

    }

    public void totalCompletedSalesBySeller(String sellerName) {

        System.out.println("Total vendas de " + sellerName + " R$: " +
                toCurrency(
                        sales.stream()
                                .filter(Sale::isCompleted)
                                .filter(s -> s.getSeller().equals(sellerName))
                                .map(Sale::getValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
        );

        imprimiSeparador();

    }

    public void countAllSalesByManager(String managerName) {

        System.out.println("Quantidade vendas da equipe de " + managerName + ": " +
                sales.stream()
                        //.filter(Sale::isCompleted)
                        .filter(s -> s.getManager().equals(managerName))
                        .collect(counting()));

        imprimiSeparador();

    }

    public void totalSalesByStatusAndMonth(Sale.Status status, Month... months) {

        sales.stream()
                .filter(s -> s.getStatus().equals(status))
                .filter(s -> Arrays.stream(months).toList().contains(s.getSaleDate().getMonth()))
                .collect(groupingBy(s -> s.getSaleDate().getMonth(), counting()))
                .entrySet().stream()
                .forEach(s -> System.out.println("Quantidade de vendas no mês de " + s.getKey() + " com status de "
                        + status + " : " + s.getValue()
                ));

        imprimiSeparador();

    }

    public void countCompletedSalesByDepartment() {

        System.out.println("Quantidade de vendas completas por departamento: ");

        sales.stream()
                .filter(Sale::isCompleted)
                .collect(groupingBy(Sale::getDepartment,
                        counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
                .forEach(s -> System.out.println(s.getKey() + " " + s.getValue() + " "));

        imprimiSeparador();

    }

    public void countCompletedSalesByPaymentMethodAndGroupingByYear() {

        System.out.println("Quantidade de vendas por método de pagamento por ano:");

        sales.stream()
                .filter(Sale::isCompleted)
                .collect(groupingBy(s -> s.getSaleDate().getYear(),
                        groupingBy(Sale::getPaymentMethod,
                                counting())))
                .entrySet()
                .stream()
                .forEach(s -> System.out.println(s.getKey() + " " + s.getValue() + " "));

        imprimiSeparador();

    }

    public void top3BestSellers() {

        System.out.println("Top 3 vendedores:");

        sales.stream()
                .filter(Sale::isCompleted)
                .collect(groupingBy(Sale::getSeller,
                        reducing(BigDecimal.ZERO, Sale::getValue, BigDecimal::add)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .forEach(s -> System.out.println(s.getKey() + " " + toCurrency(s.getValue())));

    }

    /*
     * Use esse metodo para converter objetos BigDecimal para uma represetancao de moeda
     */
    private String toCurrency(BigDecimal value) {
        return NumberFormat.getInstance().format(value);
    }

    private void imprimiSeparador() {
        System.out.println("-------------------------------------------------------------");
    }

}