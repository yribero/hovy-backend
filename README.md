# hovy-backend

## API

### Scan

/scan/{isikukood}/{phone_number}/{qr_content}

Method: GET

Triggers the authentication process. It returns some data provided by the business identified by the content of the QR code.
The wait might be long.

Example:

http://145.14.28.154:8085/scan/38211150129/+3725205452/aaa

Returns:

```
{
  "welcome": "Welcome YARY RIBERO!",
  "shop": {
    "id": 1,
    "name": "Omniva Fake Shop #1",
    "address": "Fake 11, Faketown, 11111",
    "logoUrl": "https://www.omniva.ee/theme/post24/img/logo.png"
  },
  "services": [
    {
      "index": 0,
      "name": "Collect parcel"
    },
    {
      "index": 1,
      "name": "Financial services"
    },
    {
      "index": 2,
      "name": "Send parcel"
    }
  ]
}
```

### Shop Info

/shopInfo/{qr_content}

Method: GET

It returns the data of shop associated to the QR code

Example:

http://145.14.28.154:8085/shopInfo/aaa

Returns:

```
{
  "shop": {
    "id": 1,
    "name": "Omniva Fake Shop #1",
    "address": "Fake 11, Faketown, 11111",
    "logoUrl": "https://www.omniva.ee/theme/post24/img/logo.png"
  },
  "services": [
    {
      "index": 0,
      "name": "Collect parcel"
    },
    {
      "index": 1,
      "name": "Financial services"
    },
    {
      "index": 2,
      "name": "Send parcel"
    }
  ]
}
```

### enterQueue

/enterQueue/{shopId}/{serviceId}

The parameters come from the previous call (shopId and an index from the list of services)

Method: GET

Example:

http://145.14.28.154:8085/enterQueue/1/0

Returns:

```
{
  "queueNumber": 7,
  "current": 8,
  "peopleLeft": 19
}
```

### next

/next/{shopId}/{serviceId}/{myNumber}

The parameters come from the original call (shopId and an index from the list of services)

Method: GET

Example:

http://145.14.28.154:8085/next/1/0/15

Returns:

```
{
  "queueNumber": 7,
  "current": 8,
  "peopleLeft": 19
}
```

### feedback

/feedback

Method: POST

Requires a JSON body. Example:

```
{
  "shopId": 1,
  "rate": 1,
  "comment": "Not good at all",
  "reasonType": "PERSONAL"
}
```

Allowed reason types:

        LONG_QUEUE,
        LONG_WAIT,
        WRONG_PLACE,
        PERSONAL

## Test Shops by QR code:

aaa: Omniva Fake Shop #1

bbb: New Pharmacy

ccc: Retails Store

## How to test with your phone

Register your phone number certificates at this site: https://demo.sk.ee/MIDCertsReg/, taking both steps (1 and 2). This allows the demo environment to be accessed by your phone.