# Rovizza

**Hot Pizza. Fast Delivery.**

A complete, mobile-first pizza delivery platform featuring real-time order tracking, driver management, and scalable cloud infrastructure for modern food brands.

---

## What It Is

Rovizza is a full-stack pizza delivery ecosystem — customer mobile app, driver delivery app, admin dashboard, and backend API services — all working together to deliver hot pizza fast. Built with React Native and Flask, powered by PostgreSQL with Redis and Celery for real-time operations.

---

## The Experience

**For Customers:** Browse pizzas by category, customize with toppings and crust choices, pay via card, mobile money, digital wallet, or cash. Track your order in real time from the kitchen to your doorstep with live driver location. Get push notifications at every stage.

**For Drivers:** Accept deliveries, navigate with GPS, update delivery status, track earnings. A clean interface designed for use on the move.

**For Restaurants:** Manage menus, pricing, availability across multiple branches. Monitor orders live. View revenue analytics and peak hour reports.

**Under the Hood:** Flask REST API with JWT authentication and role-based access control. Redis and Celery handle background tasks — notifications, payment processing, delivery updates. PostgreSQL stores everything with high reliability. Deployed on Railway, Vercel, and Cloudflare.

---

## The Brand

- **Primary Color:** Premium Pizza Red (#C62828)
- **Background:** Warm Cream (#FFF3E0)
- **Accent:** Cheese Gold (#FFB300)
- **Logo Font:** Outfit ExtraBold — modern, bold, rounded
- **App Font:** Poppins — clean, readable, mobile-friendly

---

## Real-Time Order Tracking

Every order moves through eight states — from placed through preparing, baking, driver assigned, out for delivery, to delivered. Customers see their driver's location, receive status updates, and get estimated arrival times. Push notifications at every stage.

---

## Built for Scale

Multiple restaurants. Multiple branches. High traffic. Real-time operations. The architecture uses queue-based processing so the system stays fast under load. Redis handles caching and real-time updates. Celery processes background tasks without blocking the API.

---

**Built by MwendaSoft** — Precision Engineering. Reliable Results.