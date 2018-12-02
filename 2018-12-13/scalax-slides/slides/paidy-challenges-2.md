## Technical challenges at Paidy

- Authorize payments as quick as possible:
  + `parTraverse` a list of computations, stop on first failure and return successes if any: "Collect Successful".
  + `parTraverse` a list of computations and result first successful value or timeout: "First Successful".

