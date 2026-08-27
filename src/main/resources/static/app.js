const API = "";
let selectedSide = "BUY";
let nextOrderID = Number(localStorage.getItem("nextOrderID") || "1");

document.querySelectorAll(".side-button").forEach(button => {
    button.addEventListener("click", () => {
        selectedSide = button.dataset.side;

        document.querySelectorAll(".side-button").forEach(item => {
            item.classList.toggle("selected", item === button);
        });
    });
});

const typeSelect = document.getElementById("type");
const priceInput = document.getElementById("price");
const priceRow = document.getElementById("price-row");

typeSelect.addEventListener("change", () => {
    const isMarket = typeSelect.value === "MARKET";

    priceInput.disabled = isMarket;
    priceInput.required = !isMarket;
    priceRow.classList.toggle("muted", isMarket);

    if (isMarket) {
        priceInput.value = "";
    }
});

document.getElementById("order-form").addEventListener("submit", async event => {
    event.preventDefault();

    const orderID = nextOrderID++;
    localStorage.setItem("nextOrderID", nextOrderID);

    const type = typeSelect.value;

    const order = {
        orderID,
        accountID: Number(document.getElementById("accountID").value),
        side: selectedSide,
        type,
        quantity: Number(document.getElementById("quantity").value),
        price: type === "MARKET" ? null : Number(priceInput.value)
    };

    try {
        const response = await fetch(`${API}/orders`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(order)
        });

        const result = await readResponse(response);

        if (!response.ok) {
            renderError(result);
            return;
        }

        renderEvents(result.eventResponses || []);

        document.getElementById("cancelOrderID").value = orderID;

        await loadOrderBook();
    } catch {
        renderError({
            message: "Could not reach the matching engine."
        });
    }
});

document.getElementById("cancel-form").addEventListener("submit", async event => {
    event.preventDefault();

    const orderID = document.getElementById("cancelOrderID").value;
    const accountID = document.getElementById("cancelAccountID").value;

    try {
        const response = await fetch(
            `${API}/orders/${orderID}?accountID=${accountID}`,
            {
                method: "DELETE"
            }
        );

        const result = await readResponse(response);

        if (!response.ok) {
            renderError(result);
            return;
        }

        renderEvents(result.eventResponses || []);

        await loadOrderBook();
    } catch {
        renderError({
            message: "Could not reach the matching engine."
        });
    }
});

async function loadOrderBook() {
    try {
        const response = await fetch(`${API}/orderbook`);
        const book = await response.json();

        document.getElementById("bids").innerHTML =
            renderLevels(book.bids);

        document.getElementById("asks").innerHTML =
            renderLevels(book.asks);
    } catch {
        document.getElementById("bids").innerHTML =
            '<div class="muted">Unable to load order book.</div>';

        document.getElementById("asks").innerHTML =
            '<div class="muted">Unable to load order book.</div>';
    }
}

function renderLevels(levels) {
    if (!levels || levels.length === 0) {
        return '<div class="muted">No orders</div>';
    }

    return levels.map(level =>
        `<div class="level">
            <span>${level.price}</span>
            <span>${level.quantity}</span>
        </div>`
    ).join("");
}

function renderEvents(events) {
    if (!events.length) {
        document.getElementById("result").innerHTML =
            '<div class="muted">No events returned.</div>';
        return;
    }

    document.getElementById("result").innerHTML = events.map(event => {
        if (event.type === "ORDER_ACCEPTED") {
            return `
                <div class="event">
                    <strong>Order Accepted</strong>
                    Order ${event.orderID} for account ${event.accountID}
                </div>
            `;
        }

        if (event.type === "TRADE_EXECUTED") {
            return `
                <div class="event">
                    <strong>Trade Executed</strong>
                    ${event.quantity} @ ${event.price}
                    (Buy ${event.buyOrderID} → Sell ${event.sellOrderID})
                </div>
            `;
        }

        if (event.type === "ORDER_CANCELLED") {
            return `
                <div class="event">
                    <strong>Order Cancelled</strong>
                    Order ${event.orderID} for account ${event.accountID}
                </div>
            `;
        }

        return `
            <div class="event">
                <strong>${event.type || "EVENT"}</strong>
                ${JSON.stringify(event)}
            </div>
        `;
    }).join("");
}

function renderError(error) {
    document.getElementById("result").innerHTML = `
        <div class="event">
            <strong>Error</strong>
            ${error.message || "Request failed."}
        </div>
    `;
}

async function readResponse(response) {
    const text = await response.text();

    if (!text) {
        return {};
    }

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
}

loadOrderBook();