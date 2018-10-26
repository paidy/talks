## Technical challenges at Paidy

- Authorize payments as quick as possible
  + For every payment authorization we need to run several assessments
  + Each assessment needs to persist a report in DB
  + Many assessments are independent of each other
  + Resilient to failures
