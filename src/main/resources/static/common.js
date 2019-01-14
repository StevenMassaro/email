function runCouponsPromise(couponsPromise){
    couponsPromise.then((successMessage) => {
        $('#coupons').DataTable({
            data: JSON.parse(successMessage),
            "columns": [
                {
                    "data": "dateReceived",
                    "width": "50px"
                },
                {
                    "data": "account",
                    "render": function(data, type, row) {
                        return row.account.username;
                    },
                    "width": "100px"
                },
                {
                    "data": "subject",
                    "render": function ( data, type, row, meta ) {
                        return '<a ' + (row.readInd ? '' : 'style="font-weight:bold" ')
                        + 'href=' + getApiBaseUrl("/body") + '?bodyId=' + row.body.id + '>' + row.subject + '</a>';
                    }
                }
            ],
            responsive: true,
            "pageLength": 50
        });
    });
}