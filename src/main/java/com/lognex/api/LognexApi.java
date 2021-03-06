package com.lognex.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lognex.api.clients.EntityClient;
import com.lognex.api.entities.AttributeEntity;
import com.lognex.api.entities.CurrencyEntity;
import com.lognex.api.entities.agents.AgentEntity;
import com.lognex.api.entities.discounts.DiscountEntity;
import com.lognex.api.entities.documents.markers.FinanceDocumentMarker;
import com.lognex.api.entities.documents.markers.FinanceInDocumentMarker;
import com.lognex.api.entities.documents.markers.FinanceOutDocumentMarker;
import com.lognex.api.entities.products.markers.ConsignmentParentMarker;
import com.lognex.api.entities.products.markers.ProductMarker;
import com.lognex.api.entities.products.markers.SingleProductMarker;
import com.lognex.api.responses.ListEntity;
import com.lognex.api.utils.json.*;
import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.time.LocalDateTime;

@Getter
public final class LognexApi {
    public static final String API_PATH = "/api/remap/1.1";
    private final String host;
    private String login;
    private String password;
    private CloseableHttpClient client;
    private boolean timeWithMilliseconds = false;
    private boolean prettyPrintJson = false;
    private boolean pricePrecision = false;
    private boolean withoutWebhookContent = false;

    /**
     * Создаёт экземпляр коннектора API
     *
     * @param host       хост, на котором располагается API
     * @param forceHttps форсировать запрос через HTTPS
     * @param login      логин пользователя
     * @param password   пароль пользователя
     */
    public LognexApi(String host, boolean forceHttps, String login, String password) {
        this(host, forceHttps, login, password, HttpClients.createDefault());
    }

    /**
     * Создаёт экземпляр коннектора API
     *
     * @param host       хост, на котором располагается API
     * @param forceHttps форсировать запрос через HTTPS
     * @param login      логин пользователя
     * @param password   пароль пользователя
     * @param client     HTTP-клиент
     */
    public LognexApi(String host, boolean forceHttps, String login, String password, CloseableHttpClient client) {
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("Адрес хоста API не может быть пустым или null!");
        host = host.trim();

        while (host.endsWith("/")) host = host.substring(0, host.lastIndexOf("/"));

        if (forceHttps) {
            if (host.startsWith("http://")) host = host.replace("http://", "https://");
            else if (!host.startsWith("https://")) host = "https://" + host;
        } else {
            if (!host.startsWith("https://") && !host.startsWith("http://")) host = "http://" + host;
        }

        this.host = host;
        this.client = client;
        setCredentials(login, password);
    }

    /**
     * Устанавливает данные доступа, которые используются для авторизации
     * запросов к API
     *
     * @param login    логин в формате <code>[имя_пользователя]@[название_компании]</code>
     * @param password пароль
     */
    public void setCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * Устанавливает пользовательский HTTP-клиент, с помощью которого будут выполняться запросы.
     */
    public void setHttpClient(CloseableHttpClient client) {
        this.client = client;
    }

    /**
     * Группа методов API, соответствующих пути <code>/entity/*</code><br/>
     * <br/>
     * <b>Внимание!</b> Внутри этой цепочки методов каждый сегмент — это отдельный объект. По
     * возможности избегайте их сохранения в переменные в долгоживущих объектах или не забывайте
     * про них, так как неосторожное использование может вызвать <b>утечку памяти</b>!<br/><br/>
     */
    public EntityClient entity() {
        return new EntityClient(this);
    }

    /**
     * Создаёт экземпляр GSON с настроенными сериализаторами и десериализаторами для
     * некоторых классов и сущностей
     */
    public static Gson createGson() {
        return createGson(false);
    }

    /**
     * Создаёт экземпляр GSON с настроенными сериализаторами и десериализаторами для
     * некоторых классов и сущностей (с возможностью настроить форматированный
     * вывод)
     */
    public static Gson createGson(boolean prettyPrinting) {
        return createGson(prettyPrinting, false);
    }

    /**
     * Создаёт экземпляр GSON с настроенными сериализаторами и десериализаторами для
     * некоторых классов и сущностей (с возможностью настроить форматированный
     * вывод и даты с миллисекундами)
     */
    public static Gson createGson(boolean prettyPrinting, boolean timeWithMilliseconds) {
        GsonBuilder gb = new GsonBuilder();

        if (prettyPrinting) {
            gb.setPrettyPrinting();
        }

        ProductMarkerSerializer pmse = new ProductMarkerSerializer();
        gb.registerTypeAdapter(ProductMarker.class, pmse);
        gb.registerTypeAdapter(SingleProductMarker.class, pmse);
        gb.registerTypeAdapter(ConsignmentParentMarker.class, pmse);

        FinanceDocumentMarkerSerializer fdms = new FinanceDocumentMarkerSerializer();
        gb.registerTypeAdapter(FinanceDocumentMarker.class, fdms);
        gb.registerTypeAdapter(FinanceInDocumentMarker.class, fdms);
        gb.registerTypeAdapter(FinanceOutDocumentMarker.class, fdms);

        gb.registerTypeAdapter(AgentEntity.class, new AgentDeserializer());
        gb.registerTypeAdapter(AttributeEntity.class, new AttributeSerializer(timeWithMilliseconds));
        gb.registerTypeAdapter(CurrencyEntity.MultiplicityType.class, new CurrencyEntity.MultiplicityType.Serializer());
        gb.registerTypeAdapter(DiscountEntity.class, new DiscountDeserializer());
        gb.registerTypeAdapter(ListEntity.class, new ListEntityDeserializer());
        gb.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer(timeWithMilliseconds));

        return gb.create();
    }

    public LognexApi timeWithMilliseconds() {
        return timeWithMilliseconds(true);
    }

    public LognexApi timeWithMilliseconds(boolean value) {
        this.timeWithMilliseconds = value;
        return this;
    }

    public LognexApi prettyPrintJson() {
        return prettyPrintJson(true);
    }

    public LognexApi prettyPrintJson(boolean value) {
        this.prettyPrintJson = value;
        return this;
    }

    public LognexApi precision() {
        return precision(true);
    }

    public LognexApi precision(boolean value) {
        this.pricePrecision = value;
        return this;
    }

    public LognexApi withoutWebhookContent() {
        return withoutWebhookContent(true);
    }

    public LognexApi withoutWebhookContent(boolean without) {
        this.withoutWebhookContent = without;
        return this;
    }
}
