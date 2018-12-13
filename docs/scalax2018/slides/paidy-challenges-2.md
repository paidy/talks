## Technical challenges at Paidy

- Authorize payments as quick as possible:
  + `parTraverse` a list of computations, stop on first failure and return successes if any: "Collect Successful".
  + `parTraverse` a list of computations and get first successful value or timeout otherwise: "First Successful".

